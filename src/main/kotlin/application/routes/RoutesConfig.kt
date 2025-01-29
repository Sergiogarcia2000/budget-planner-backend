package application.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import domain.modules.users.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userService: UserService by inject()
    routing {
        v1(userService)
    }
}
