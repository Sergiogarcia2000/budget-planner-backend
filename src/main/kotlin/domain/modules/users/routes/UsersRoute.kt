package domain.modules.users.routes

import application.responses.ErrorResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import domain.modules.users.models.CreateUserRequest
import domain.modules.users.models.UpdateUserRequest
import domain.modules.users.services.UserService

fun Route.userRoutes(userService: UserService) {
    route("/users") {

        get {
            call.respond(userService.getAllUsers())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Bad Request", "Missing user id"))

            val user = userService.getUserById(id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("Not found", "User with id $id not found")
                )

            call.respond(user)
        }

        post {
            val request = call.receive<CreateUserRequest>()

            userService.createUser(request).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        patch("/{id}") {
            val request = call.receive<UpdateUserRequest>()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, ErrorResponse("Bad Request", "Missing user id"))

            userService.updateUser(id, request).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing user id")
                )

            userService.deleteUser(id).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent, it) },
                onFailure = { throw it }
            )
        }
    }
}