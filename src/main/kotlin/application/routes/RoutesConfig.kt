package application.routes

import domain.modules.auth.services.AuthService
import domain.modules.budgets.services.BudgetsService
import domain.modules.categories.services.CategoryService
import domain.modules.expenses.services.ExpensesService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import domain.modules.users.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userService by inject<UserService>()
    val authService by inject<AuthService>()
    val categoryService by inject<CategoryService>()
    val expensesService by inject<ExpensesService>()
    val budgetsService by inject<BudgetsService>()

    routing {
        v1(userService, authService, categoryService, expensesService, budgetsService)
    }
}
