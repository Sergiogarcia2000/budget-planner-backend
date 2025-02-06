package domain.modules.budgets.routes

import application.extensions.getBaseFilter
import application.extensions.getUserId
import application.extensions.respondBadRequest
import domain.modules.budgets.models.budget.BudgetFilter
import domain.modules.budgets.models.budget.CreateBudgetRequest
import domain.modules.budgets.models.budget.UpdateBudgetRequest
import domain.modules.budgets.routes.budgetCategories.budgetCategoriesRoute
import domain.modules.budgets.services.BudgetCategoriesService
import domain.modules.budgets.services.BudgetService
import domain.modules.budgets.services.BudgetSummaryService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import java.time.LocalDate

fun Route.budgetsRoute() {
    val budgetService: BudgetService by application.inject<BudgetService>()
    val budgetCategoriesService: BudgetCategoriesService by application.inject<BudgetCategoriesService>()
    val budgetSummaryService: BudgetSummaryService by application.inject<BudgetSummaryService>()

    route("/budgets") {

        get {
            val userId = call.getUserId()
            val minLimit = call.request.queryParameters["minLimit"]?.toIntOrNull()
            val maxLimit = call.request.queryParameters["maxLimit"]?.toIntOrNull()
            val startDate = call.request.queryParameters["startDate"]?.let { LocalDate.parse(it) }
            val endDate = call.request.queryParameters["endDate"]?.let { LocalDate.parse(it) }
            val recurrent = call.request.queryParameters["recurrent"]?.toBoolean()
            val finished = call.request.queryParameters["finished"]?.toBoolean()

            val baseFilter = call.getBaseFilter()

            val filter = BudgetFilter(
                userId = userId,
                minLimit = minLimit,
                maxLimit = maxLimit,
                startDate = startDate,
                endDate = endDate,
                recurrent = recurrent,
                finished = finished,
                orderBy = baseFilter.orderBy,
                orderDirection = baseFilter.orderDirection,
                page = baseFilter.page,
                pageSize = baseFilter.pageSize
            )

            val budgets = budgetService.getAllBudgets(filter)
            call.respond(HttpStatusCode.OK, budgets)
        }

        get("/{id}") {
            val userId = call.getUserId()
            val budgetId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondBadRequest("Budget")

            budgetService.getBudget(budgetId = budgetId, userId = userId).fold(
                onSuccess = {  call.respond(HttpStatusCode.OK, it) },
                onFailure = { throw it }
            )
        }

        post {
            val userId = call.getUserId()
            val budgetRequest = call.receive<CreateBudgetRequest>()

            budgetService.create(userId = userId, request = budgetRequest).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        patch("/{id}") {
            val userId = call.getUserId()
            val budgetId = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respondBadRequest("Budget")

            val request = call.receive<UpdateBudgetRequest>()

            budgetService.update(budgetId = budgetId, userId = userId, request = request).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }

        delete("/{id}") {
            val userId = call.getUserId()
            val budgetId = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respondBadRequest("Budget")

            budgetService.delete(budgetId = budgetId, userId = userId).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }

        get("/summary") {
            val userId = call.getUserId()

            val summaries = budgetSummaryService.getBudgetsSummaries(userId = userId)
            call.respond(HttpStatusCode.OK, summaries)
        }

        get("/{id}/summary") {
            val userId = call.getUserId()
            val budgetId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondBadRequest("Budget")

            budgetSummaryService.getBudgetSummary(budgetId = budgetId, userId = userId).fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { throw it }
            )
        }

        budgetCategoriesRoute(budgetCategoriesService)
    }
}