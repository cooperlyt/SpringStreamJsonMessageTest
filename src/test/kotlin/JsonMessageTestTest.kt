package io.github.cooperlyt

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.convert.ConversionService
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [Application::class])
class JsonMessageTestTest {

    @Autowired
    private lateinit var jsonMessageTest: JsonMessageTest


    @Test
    fun testMessage() {

        StepVerifier.create(jsonMessageTest.sendMessage())
            .expectSubscription()
            .expectNext(MyBean("foo"))
            .verifyComplete()
    }
}
