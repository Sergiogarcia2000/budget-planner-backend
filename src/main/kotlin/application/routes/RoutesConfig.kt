package application.routes

import domain.modules.auth.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import domain.modules.users.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userService: UserService by inject()
    val authService: AuthService by inject()
    routing {
        v1(userService, authService)
    }
}
