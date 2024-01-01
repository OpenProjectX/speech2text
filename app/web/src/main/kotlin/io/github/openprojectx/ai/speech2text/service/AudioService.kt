package io.github.openprojectx.ai.speech2text.service

import com.lordcodes.turtle.shellRun
import fr.noop.subtitle.srt.SrtCue
import fr.noop.subtitle.srt.SrtObject
import fr.noop.subtitle.srt.SrtParser
import fr.noop.subtitle.srt.SrtWriter
import fr.noop.subtitle.util.SubtitleTimeCode
import io.github.openprojectx.ai.speech2text.client.OpenAIClient
import io.micrometer.tracing.Tracer
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import reactor.core.scheduler.Schedulers
import java.util.stream.Collector
import java.util.stream.Collectors
import kotlin.text.Charsets.UTF_8

@Service
class AudioService(
    private val openAIClient: OpenAIClient,
    private val tracer: Tracer,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val srtParser: SrtParser = SrtParser("utf-8")

    private val srtWriter = SrtWriter(UTF_8.name())


    fun audioToText(audio: FilePart): Mono<String> {
        val traceId = tracer.currentSpan()!!.context().traceId()
        val tmpFileDir = "/tmp/speech2text/$traceId"
        val tmpFilePath = "$tmpFileDir/${audio.filename()}"
        val tmpFile = File(tmpFilePath)
        FileUtils.touch(tmpFile)

        return audio.transferTo(tmpFile).then(Mono.fromCallable {
            val result = shellRun {
                command(
                    "ffmpeg",
                    listOf(
                        "-i", tmpFilePath,
                        "-map", "0:a",
                        "-f", "segment",
                        "-segment_time", "1200",
                        "-reset_timestamps", "1",
                        "-c",
                        "copy", "$tmpFileDir/%03d-${audio.filename()}.m4a"
                    )
                )

            }
            tmpFile.delete()
            log.info("transform result\n$result")
            (File(tmpFileDir).listFiles() ?: throw RuntimeException()).asList()
                .sortedBy { it.name }
                .also {
                    log.info("files to be uploaded {}", it.joinToString(", ") { it.name })
                }
                .mapIndexed { index, file ->
                    Pair(index, file)
                }
        })
            .flatMapIterable { it }
            .publishOn(Schedulers.single())
            .subscribeOn(Schedulers.single())
            .flatMap({ (index, chunkFile) ->
                log.info("uploading chunkFile {}", chunkFile)
                openAIClient.audioToTranslations(audio = FileSystemResource(chunkFile))
                    .map {
                        Pair(index, it)
                    }
            }, 1)
            .collectList()
            .map { indexedTranscriptions ->
                indexedTranscriptions
                    .asSequence()
                    .map { (index, srt) ->
                        val srtObject = srtParser.parse(srt.byteInputStream())
                        srtObject!!.cues!!.forEach {
                            val srtCue = it as SrtCue
                            srtCue.startTime = SubtitleTimeCode(
                                srtCue.startTime.time + index * Duration.ofSeconds(1200).toMillis()
                            )
                            srtCue.endTime = SubtitleTimeCode(
                                srtCue.endTime.time + index * Duration.ofSeconds(1200).toMillis()
                            )
                        }

                        Pair(index, srtObject)
                    }
                    .sortedBy {
                        it.first
                    }
                    .also {
                        log.info("srt indexes {}", it.joinToString(", ") { it.first.toString() })
                    }
                    .map {
                        it.second
                    }
                    .foldIndexed(ArrayList<SrtObject>()) { index, acc, srtObject ->

                        acc.getOrNull(index - 1)?.cues?.last()?.id?.toInt()?.run {
                            srtObject.cues.forEach {
                                val srtCue = it as SrtCue
                                srtCue.id = (it.id.toInt() + this).toString()
                            }
                        }
                        acc.also {
                            it.add(srtObject)
                        }
                    }.joinToString("\n") { srtObject ->
                        ByteArrayOutputStream().use {
                            srtWriter.write(srtObject, it)
                            it.toString(UTF_8)
                        }
                    }
                    .also {
                        File("$tmpFileDir/${audio.filename()}.srt").writeText(it)
                    }
            }
    }
}