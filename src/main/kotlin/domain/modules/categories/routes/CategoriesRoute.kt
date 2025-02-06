package domain.modules.categories.routes

import application.responses.ErrorResponse
import domain.modules.categories.models.CategoryRequest
import domain.modules.categories.services.CategoryService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.categoriesRoute() {
    val categoryService: CategoryService by application.inject<CategoryService>()

    route("/categories") {

        get {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            call.respond(HttpStatusCode.OK, categoryService.getAllCategories(userId))
        }

        get("/{id}") {
            val categoryId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing category ID")
                )

            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            val category = categoryService.getCategoryById(categoryId, userId)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("Not Found", "Category with id $categoryId not found")
                )

            call.respond(HttpStatusCode.OK, category)
        }

        get("/{id}/budgets") {
            val categoryId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing category ID")
                )
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            categoryService.getCategoryBudgets(categoryId = categoryId, userId = userId).fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { throw it }
            )
        }

        post {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
            val request = call.receive<CategoryRequest>()

            categoryService.create(userId, request).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        patch("/{id}") {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
            val categoryId = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing category ID")
                )

            val request = call.receive<CategoryRequest>()

            categoryService.updateCategory(userId, categoryId, request).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        delete("/{id}") {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
            val categoryId = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing category ID")
                )

            categoryService.deleteCategory(categoryId, userId).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }

    }
}