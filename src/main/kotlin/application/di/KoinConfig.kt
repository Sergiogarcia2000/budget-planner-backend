package application.di

import data.database.DbManager
import domain.modules.auth.services.AuthService
import domain.modules.categories.repositories.CategoryRepository
import domain.modules.categories.services.CategoryService
import domain.modules.expenses.repositories.ExpensesRepository
import domain.modules.expenses.services.ExpensesService
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
                single { ExpensesRepository() }

                single { AuthService(get()) }
                single { UserService(get()) }
                single { CategoryService(get()) }
                single { ExpensesService(get()) }
            }
        )
    }
}