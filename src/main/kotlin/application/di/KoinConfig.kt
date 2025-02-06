package application.di

import data.database.DbManager
import domain.modules.auth.services.AuthService
import domain.modules.budgets.repositories.BudgetRepository
import domain.modules.budgets.services.BudgetCategoriesService
import domain.modules.budgets.services.BudgetService
import domain.modules.budgets.services.BudgetSummaryService
import domain.modules.categories.repositories.CategoryRepository
import domain.modules.categories.services.CategoryService
import domain.modules.expenses.repositories.ExpenseRepository
import domain.modules.expenses.services.ExpenseService
import io.ktor.server.application.*
import domain.modules.users.repositories.UserRepository
import domain.modules.users.services.UserService
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger(level = Level.ERROR)
        modules(
            module {
                single { DbManager.start() }
                single { UserRepository() }
                single { CategoryRepository() }
                single { ExpenseRepository() }
                single { BudgetRepository() }

                single { AuthService(get()) }
                single { UserService(get()) }
                single { CategoryService(get()) }
                single { ExpenseService(get(), get()) }
                single { BudgetService(get()) }
                single { BudgetCategoriesService(get(), get()) }
                single { BudgetSummaryService(get(), get()) }
            }
        )
    }
}