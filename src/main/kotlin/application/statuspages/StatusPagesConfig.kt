package application.statuspages

import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import application.models.ErrorResponse
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {

    val logger = LoggerFactory.getLogger("StatusPagesLogger")

    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Validationasd Error", "message" to cause.errors))
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse("Not found", cause.message ?: "Entity not found"))
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation Error", cause.message ?: "Invalid input"))
        }
        exception<Throwable> { call, cause ->
            logger.error("Error inesperado", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected Error", cause.stackTraceToString()))
        }
    }
}