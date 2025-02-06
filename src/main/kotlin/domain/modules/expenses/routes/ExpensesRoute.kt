package domain.modules.expenses.routes

import application.responses.ErrorResponse
import domain.modules.expenses.models.CreateExpenseRequest
import domain.modules.expenses.models.ExpenseFilter
import domain.modules.expenses.models.UpdateExpenseRequest
import domain.modules.expenses.services.ExpenseService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import java.time.LocalDateTime

fun Route.expensesRoute() {
    val expenseService: ExpenseService by application.inject()

    route("/expenses") {

        get {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!
            val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()
            val minAmount = call.request.queryParameters["minAmount"]?.toDoubleOrNull()
            val maxAmount = call.request.queryParameters["maxAmount"]?.toDoubleOrNull()
            val startDate = call.request.queryParameters["startDate"]?.let { LocalDateTime.parse(it) }
            val endDate = call.request.queryParameters["endDate"]?.let { LocalDateTime.parse(it) }
            val orderBy = call.request.queryParameters["orderBy"]
            val orderDirection = call.request.queryParameters["orderDirection"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

            val filter = ExpenseFilter(
                userId = userId,
                categoryId = categoryId,
                minAmount = minAmount,
                maxAmount = maxAmount,
                startDate = startDate,
                endDate = endDate,
                orderBy = orderBy,
                orderDirection = orderDirection,
                page = page,
                pageSize = pageSize
            )
            val expenses = expenseService.getAllExpenses(filter)
            call.respond(HttpStatusCode.OK, expenses)
        }

        get("/{id}") {
            val expenseId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing expense ID")
                )

            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            val expense = expenseService.getExpense(expenseId, userId)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Not found", "Expense not found"))

            call.respond(HttpStatusCode.OK, expense)
        }

        post {
            val expense = call.receive<CreateExpenseRequest>()
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            expenseService.create(userId, expense).fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { throw it }
            )
        }

        patch("/{id}") {
            val expenseId = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing expense ID")
                )
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            val request = call.receive<UpdateExpenseRequest>()

            expenseService.update(userId, expenseId, request).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }

        delete("/{id}") {
            val expenseId = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Bad Request", "Missing expense ID")
                )
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

            expenseService.delete(userId, expenseId).fold(
                onSuccess = { call.respond(HttpStatusCode.NoContent) },
                onFailure = { throw it }
            )
        }
    }
}