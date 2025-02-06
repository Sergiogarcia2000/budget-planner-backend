package application.routes

import application.auth.SecurityConfig.AUTH_JWT
import application.websockets.webSocketRoutes
import domain.modules.auth.routes.authRoutes
import domain.modules.budgets.routes.budgetsRoute
import domain.modules.categories.routes.categoriesRoute
import domain.modules.expenses.routes.expensesRoute
import io.ktor.server.routing.*
import domain.modules.users.routes.userRoutes
import io.ktor.server.auth.*


fun Routing.v1() {
    route("/v1") {
        authRoutes()
        authenticate(AUTH_JWT) {
            userRoutes()
            categoriesRoute()
            expensesRoute()
            budgetsRoute()
        }
        webSocketRoutes()
    }
}