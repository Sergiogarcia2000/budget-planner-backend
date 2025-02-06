package application

import EnvironmentHandler
import application.auth.SecurityConfig.configureSecurity
import application.di.configureKoin
import application.routes.configureRouting
import com.fasterxml.jackson.databind.SerializationFeature
import application.statuspages.configureStatusPages
import data.database.DbManager
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.*
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    EnvironmentHandler.init(this)
    DbManager.start()

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    configureKoin()
    configureSecurity()
    configureRouting()
    configureStatusPages()



    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }
    install(Compression)
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
}
