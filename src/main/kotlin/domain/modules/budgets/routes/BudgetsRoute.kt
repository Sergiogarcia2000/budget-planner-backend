package domain.modules.budgets.routes

import application.responses.ErrorResponse
import domain.modules.budgets.models.BudgetCategoriesRequest
import domain.modules.budgets.models.BudgetFilter
import domain.modules.budgets.models.CreateBudgetRequest
import domain.modules.budgets.models.UpdateBudgetRequest
import domain.modules.budgets.services.BudgetsService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import java.time.LocalDate

fun Route.budgetsRoute(budgetsService: BudgetsService) {
    route("/budgets") {

        get {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
            val minLimit = call.request.queryParameters["minLimit"]?.toIntOrNull()
            val maxLimit = call.request.queryParameters["maxLimit"]?.toIntOrNull()
            val startDate = call.request.queryParameters["startDate"]?.let { LocalDate.parse(it) }
            val endDate = call.request.queryParameters["endDate"]?.let { LocalDate.parse(it) }
            val recurrent = call.request.queryParameters["recurrent"]?.toBoolean()
            val finished = call.request.queryParameters["finished"]?.toBoolean()
            val orderBy = call.request.queryParameters["orderBy"]
            val orderDirection = call.request.queryParameters["orderDirection"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

            val filter = BudgetFilter(
                userId = userId,
                minLimit = minLimit,
                maxLimit = maxLimit,
                startDate = startDate,
                endDate = endDate,
                recurrent = recurrent,
                finished = finished,
                orderBy = orderBy,
                orderDirection = orderDirection,
                page = page,
                pageSize = pageSize
            )

            val budgets = budgetsService.getAllBudgets(filter)
            call.respond(HttpStatusCode.OK, budgets)
        }

        get("/{id}") {
            val budgetId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing budget ID")
                )
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            val budget = budgetsService.getBudget(budgetId = budgetId, userId = userId)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound", "Budget not found"))

            call.respond(HttpStatusCode.OK, budget)
        }

        post {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
            val budgetRequest = call.receive<CreateBudgetRequest>()

            budgetsService.create(userId = userId, request = budgetRequest).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        patch("/{id}") {
            val budgetId = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing budget ID")

            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
            val request = call.receive<UpdateBudgetRequest>()

            budgetsService.update(budgetId = budgetId, userId = userId, request = request).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }

        delete("/{id}") {
            val budgetId = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing Budget ID")
                )
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            budgetsService.delete(budgetId = budgetId, userId = userId).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }

        route("/{id}/categories") {

            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
                val budgetId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing Budget ID")
                )

                budgetsService.getBudgetCategories(budgetId, userId).fold(
                    onSuccess = { call.respond(HttpStatusCode.OK, it) },
                    onFailure = { throw it }
                )
            }

            patch {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
                val budgetId = call.parameters["id"]?.toIntOrNull() ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing Budget ID")
                )
                val budgetCategories = call.receive<BudgetCategoriesRequest>()

                budgetsService.addBudgetCategories(budgetId = budgetId, userId = userId, request = budgetCategories).fold(
                    onSuccess = { call.respond(HttpStatusCode.OK, it) },
                    onFailure = { throw it }
                )
            }

            delete {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
                val budgetId = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing Budget ID")
                )
                val budgetCategories = call.receive<BudgetCategoriesRequest>()

                budgetsService.removeBudgetCategories(budgetId = budgetId, userId = userId, request = budgetCategories ).fold(
                    onSuccess = { call.respond(HttpStatusCode.NoContent) },
                    onFailure = { throw it }
                )
            }
        }
    }
}