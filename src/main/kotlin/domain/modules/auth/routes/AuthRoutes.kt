package domain.modules.auth.routes

import domain.modules.auth.models.UserLogin
import domain.modules.auth.services.AuthService
import domain.modules.users.models.CreateUserRequest
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<CreateUserRequest>()

            authService.register(request).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, mapOf("token" to it)) },
                onFailure = { throw it }
            )
        }

        post("/login") {
            val request = call.receive<UserLogin>()

            authService.login(request).fold(
                onSuccess = { call.respond(HttpStatusCode.OK, mapOf("token" to it)) },
                onFailure = { throw it }
            )
        }
    }
}