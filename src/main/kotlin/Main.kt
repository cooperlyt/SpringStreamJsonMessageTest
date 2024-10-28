package io.github.cooperlyt

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.converter.MessageConverter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.function.Consumer
import java.util.function.Supplier


@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

data class MyBean(var string: String?)

@RestController
@RequestMapping("/test/")
class StockInternalController(private val jsonMessageTest: JsonMessageTest) {

    @GetMapping("json")
    fun test(): Mono<MyBean> {
        return jsonMessageTest.sendMessage()
    }
}


class CustomMessageMarshallingConverter(objectMapper: ObjectMapper) : MappingJackson2MessageConverter(objectMapper)

@Configuration
class RabbitMQAutoConfigure {

//    @Bean
//    fun messageConverter(objectMapper: ObjectMapper): MessageConverter {
//        return CustomMessageMarshallingConverter(objectMapper)
//    }
}

@Service
class JsonMessageTest {

    companion object {
        private val logger = LoggerFactory.getLogger(JsonMessageTest::class.java)
    }
    private val responseSink: Sinks.One<MyBean> = Sinks.one()


    private val sinks: Sinks.Many<Message<MyBean>> = Sinks.many().multicast().onBackpressureBuffer()


    protected fun sinks() : Supplier<Flux<Message<MyBean>>> {
        return Supplier { with(sinks) { asFlux() } }
    }


    @Bean
    fun testProducer() = sinks()

    fun sendMessage(): Mono<MyBean> {

        sinks.tryEmitNext(MessageBuilder.withPayload(MyBean("foo")).build()).isSuccess

        return responseSink.asMono()
    }

    @Bean
    fun testConsumer(): Consumer<Message<MyBean>> {
        return Consumer<Message<MyBean>> { originMessage ->
            logger.info("receive a message ${originMessage.payload}")
            responseSink.tryEmitValue(originMessage.payload)
        }
    }


}