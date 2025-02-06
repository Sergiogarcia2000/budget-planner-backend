package application.extensions

import application.models.BaseFilter
import application.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

fun RoutingCall.getUserId(): Int = this.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

fun RoutingCall.respondBadRequest(paramName: String) {
    launch {
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse("Bad Request", "Missing $paramName ID")
        )
    }
}

fun RoutingCall.respondNotFount(paramName: String, id: Int) {
    launch {
        respond(
            HttpStatusCode.NotFound,
            ErrorResponse("Bad Request", "$paramName with ID $id not found")
        )
    }
}

fun RoutingCall.getBaseFilter(): BaseFilter {
    return BaseFilter(
        orderBy = request.queryParameters["orderBy"],
        orderDirection = request.queryParameters["orderDirection"],
        startDate = request.queryParameters["startDate"]?.let { LocalDateTime.parse(it) },
        endDate = request.queryParameters["endDate"]?.let { LocalDateTime.parse(it) },
        page = request.queryParameters["page"]?.toIntOrNull() ?: 1,
        pageSize = request.queryParameters["pageSize"]?.toIntOrNull() ?: 10
    )
}