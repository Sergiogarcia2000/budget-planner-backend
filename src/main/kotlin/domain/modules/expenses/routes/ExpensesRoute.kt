package domain.modules.expenses.routes

import application.extensions.getBaseFilter
import application.extensions.getUserId
import application.extensions.respondBadRequest
import domain.modules.expenses.models.CreateExpenseRequest
import domain.modules.expenses.models.ExpenseFilter
import domain.modules.expenses.models.UpdateExpenseRequest
import domain.modules.expenses.services.ExpenseService
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

fun Route.expensesRoute() {
    val expenseService: ExpenseService by application.inject()

    route("/expenses") {

        get {
            val userId = call.getUserId()
            val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()
            val minAmount = call.request.queryParameters["minAmount"]?.toDoubleOrNull()
            val maxAmount = call.request.queryParameters["maxAmount"]?.toDoubleOrNull()

            val baseFilter = call.getBaseFilter()

            val filter = ExpenseFilter(
                userId = userId,
                categoryId = categoryId,
                minAmount = minAmount,
                maxAmount = maxAmount,
                startDate = baseFilter.startDate,
                endDate = baseFilter.endDate,
                orderBy = baseFilter.orderBy,
                orderDirection = baseFilter.orderDirection,
                page = baseFilter.page,
                pageSize = baseFilter.pageSize
            )
            val expenses = expenseService.getAllExpenses(filter)
            call.respond(HttpStatusCode.OK, expenses)
        }

        get("/{id}") {
            val userId = call.getUserId()
            val expenseId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondBadRequest("Expenses")

            expenseService.getExpense(expenseId, userId).fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { throw it }
            )
        }

        post {
            val userId = call.getUserId()
            val expense = call.receive<CreateExpenseRequest>()

            expenseService.create(userId, expense).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        patch("/{id}") {
            val userId = call.getUserId()
            val expenseId = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respondBadRequest("Expenses")

            val request = call.receive<UpdateExpenseRequest>()

            expenseService.update(userId, expenseId, request).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }

        delete("/{id}") {
            val userId = call.getUserId()
            val expenseId = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respondBadRequest("Expenses")

            expenseService.delete(userId, expenseId).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }
    }
}