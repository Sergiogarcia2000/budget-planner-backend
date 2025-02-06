package domain.modules.budgets.routes.budgetCategories

import application.extensions.getUserId
import application.responses.ErrorResponse
import domain.modules.budgets.models.budgetCategories.BudgetCategoriesRequest
import domain.modules.budgets.services.BudgetCategoriesService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.budgetCategoriesRoute(budgetCategoriesService: BudgetCategoriesService) {
    route("/{id}/categories") {

        get {
            val userId = call.getUserId()
            val budgetId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Bad Request", "Missing Budget ID")
            )

            budgetCategoriesService.getBudgetCategories(budgetId, userId).fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { throw it }
            )
        }

        patch {
            val userId = call.getUserId()
            val budgetId = call.parameters["id"]?.toIntOrNull() ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Bad Request", "Missing Budget ID")
            )
            val budgetCategories = call.receive<BudgetCategoriesRequest>()

            budgetCategoriesService.addBudgetCategories(budgetId = budgetId, userId = userId, request = budgetCategories).fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { throw it }
            )
        }

        delete {
            val userId = call.getUserId()
            val budgetId = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Bad Request", "Missing Budget ID")
            )
            val budgetCategories = call.receive<BudgetCategoriesRequest>()

            budgetCategoriesService.removeBudgetCategories(budgetId = budgetId, userId = userId, request = budgetCategories ).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }
    }
}