package io.github.openprojectx.ai.speech2text.resource.v1

import io.github.openprojectx.ai.speech2text.service.AudioService
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/audio")
class AudioController(
    private val audioService: AudioService
) {

    @PostMapping("/transcriptions", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun speechToText(@RequestPart audio: FilePart) :Mono<String>{
       return audioService.audioToText(audio)
    }
}