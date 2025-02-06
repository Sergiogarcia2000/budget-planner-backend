package application.websockets

import application.auth.JwtConfig
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.webSocketRoutes() {

    webSocket("/websocket") {
        val token = call.request.queryParameters["token"]
            ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing token"))

        val principal = JwtConfig.validateToken(token)
            ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))

        val userId = principal.payload.getClaim("id")?.asInt()
            ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid userId"))

        WebSocketManager.addConnection(userId, this)

        try {
            incoming.consumeEach { _ ->
            }
        } catch (e: Exception) {
            println("WebSocket error: ${e.localizedMessage}")
        } finally {
            WebSocketManager.removeConnection(userId, this)
        }
    }
}
