package domain.modules.users.routes

import application.extensions.respondBadRequest
import application.extensions.respondNotFound
import application.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import domain.modules.users.models.CreateUserRequest
import domain.modules.users.models.UpdateUserRequest
import domain.modules.users.services.UserService
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userService: UserService by application.inject<UserService>()

    route("/users") {

        get {
            call.respond(userService.getAllUsers())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondBadRequest("User")

            val user = userService.getUserById(id)
                ?: return@get call.respondNotFound("User", id)

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