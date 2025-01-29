package application.routes

import io.ktor.server.routing.*
import domain.modules.users.routes.userRoutes
import domain.modules.users.services.UserService

fun Routing.v1(userService: UserService) {
    route("/v1") {
        userRoutes(userService)
    }
}