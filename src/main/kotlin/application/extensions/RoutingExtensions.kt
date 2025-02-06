package application.extensions

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*

fun RoutingCall.getUserId(): Int = this.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!