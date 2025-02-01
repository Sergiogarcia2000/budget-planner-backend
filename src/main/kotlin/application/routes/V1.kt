package application.routes

import application.auth.SecurityConfig.AUTH_JWT
import domain.modules.auth.routes.authRoutes
import domain.modules.auth.services.AuthService
import io.ktor.server.routing.*
import domain.modules.users.routes.userRoutes
import domain.modules.users.services.UserService
import io.ktor.server.auth.*

fun Routing.v1(userService: UserService, authService: AuthService) {
    route("/v1") {
        authRoutes(authService)
        authenticate(AUTH_JWT) {
            userRoutes(userService)
        }
    }
}