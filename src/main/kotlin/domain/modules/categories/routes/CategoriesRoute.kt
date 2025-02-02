package domain.modules.categories.routes

import domain.modules.categories.services.CategoryService
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoriesRoute(categoryService: CategoryService) {

    route("/categories") {

        get {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("id").asInt()

            call.respond(categoryService.getAllCategories(userId))
        }

    }
}