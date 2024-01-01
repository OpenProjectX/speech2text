package io.github.openprojectx.ai.speech2text.client

import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import reactor.core.publisher.Mono


@HttpExchange(url = "/v1")
interface OpenAIClient {

    @PostExchange("/audio/transcriptions")
    fun audioToTranscriptions(
        @RequestPart("model") model: String = "whisper-1",
        @RequestPart("response_format") responseFormat: String = "srt",
        @RequestPart("file") audio: Resource
    ): Mono<String>

    @PostExchange("/audio/translations")
    fun audioToTranslations(
        @RequestPart("model") model: String = "whisper-1",
        @RequestPart("response_format") responseFormat: String = "srt",
        @RequestPart("file") audio: Resource
    ): Mono<String>
}