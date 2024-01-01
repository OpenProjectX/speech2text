package io.github.openprojectx.ai.speech2text.config

import io.github.openprojectx.ai.speech2text.client.OpenAIClient
import io.netty.channel.ChannelOption
import io.netty.channel.epoll.EpollChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.transport.ProxyProvider
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.TimeUnit


@Configuration
class AppConfig {

    @Bean
    fun openAIClient(
        @Value("\${open-ai.base-url}") baseUrl: String,
        @Value("\${open-ai.token}") token: String
    ): OpenAIClient {


        val client = WebClient.builder()
            .apply { builder ->
                builder.baseUrl(baseUrl)
                builder.filter(ExchangeFilterFunctions.basicAuthentication())
                builder.defaultHeaders {
                    it.setBearerAuth(token)
                }
                val provider = ConnectionProvider.builder("custom")
                    .maxConnections(64)
                    .maxIdleTime(Duration.ofMinutes(160))
                    .maxLifeTime(Duration.ofMinutes(600))
                    .pendingAcquireTimeout(Duration.ofMinutes(600))
                    .evictInBackground(Duration.ofMinutes(1200))
                    .build()

                builder.clientConnector(ReactorClientHttpConnector(HttpClient.create(provider)
                    .proxy {
                        it.type(ProxyProvider.Proxy.HTTP)
                            .address(InetSocketAddress("127.0.0.1", 10809))
                            .connectTimeoutMillis(1000 * 60 * 16 * 10)
                    }
                    .doOnConnected {
                        it.addHandlerFirst(ReadTimeoutHandler(100, TimeUnit.MINUTES))
                            .addHandlerFirst(WriteTimeoutHandler(100, TimeUnit.MINUTES))
                    }
//                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000000)
//                    .option(ChannelOption.SO_KEEPALIVE, true)
//                    .option(EpollChannelOption.TCP_KEEPIDLE, 10000)
//                    .option(EpollChannelOption.TCP_KEEPINTVL, 1000)
//                    .option(EpollChannelOption.TCP_KEEPCNT, 100)
                    .responseTimeout(Duration.ofMinutes(160))
                ))
            }
            .build()

        return HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(client))
            .build()
            .createClient(OpenAIClient::class.java)

    }
}