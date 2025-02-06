package domain.modules.categories.routes

import application.extensions.getBaseFilter
import application.extensions.getUserId
import application.extensions.respondBadRequest
import domain.modules.categories.models.CategoryRequest
import domain.modules.categories.services.CategoryService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.categoriesRoute() {
    val categoryService: CategoryService by application.inject<CategoryService>()

    route("/categories") {

        get {
            val userId = call.getUserId()

            val baseFilter = call.getBaseFilter()

            call.respond(HttpStatusCode.OK, categoryService.getAllCategories(userId, baseFilter))
        }

        get("/{id}") {
            val userId = call.getUserId()
            val categoryId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondBadRequest("Category")

            categoryService.getCategoryById(categoryId, userId).fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { call.respond(HttpStatusCode.BadRequest) }
            )
        }

        get("/{id}/budgets") {
            val userId = call.getUserId()
            val categoryId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondBadRequest("Category")

            categoryService.getCategoryBudgets(categoryId = categoryId, userId = userId).fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { throw it }
            )
        }

        post {
            val userId = call.getUserId()
            val request = call.receive<CategoryRequest>()

            categoryService.create(userId, request).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        patch("/{id}") {
            val userId = call.getUserId()
            val categoryId = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respondBadRequest("Category")

            val request = call.receive<CategoryRequest>()

            categoryService.updateCategory(userId, categoryId, request).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        delete("/{id}") {
            val userId = call.getUserId()
            val categoryId = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respondBadRequest("Category")

            categoryService.deleteCategory(categoryId, userId).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }

    }
}