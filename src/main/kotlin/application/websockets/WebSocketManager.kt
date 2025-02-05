package application.websockets

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.concurrent.ConcurrentHashMap

object WebSocketManager {

    private val connections = ConcurrentHashMap<Int, MutableSet<DefaultWebSocketServerSession>>()
    private val mutex = Mutex()

    suspend fun addConnection(userId: Int, session: DefaultWebSocketServerSession) {
        mutex.withLock {
            connections.computeIfAbsent(userId) { mutableSetOf() }.add(session)
        }
    }

    suspend fun removeConnection(userId: Int, session: DefaultWebSocketServerSession) {
        mutex.withLock {
            connections[userId]?.remove(session)
            if (connections[userId]?.isEmpty() == true) {
                connections.remove(userId)
            }
        }
    }

    suspend fun sendEventToUser(userId: Int, jsonEvent: String) {
        mutex.withLock {
            connections[userId]?.forEach { session ->
                session.send(jsonEvent)
            }
        }
    }

    suspend inline fun <reified T> sendEvent(userId: Int, entityType: String, action: String, data: T) {
        val serializer: KSerializer<WebSocketEvent<T>> = serializer()
        val event = WebSocketEvent(entityType, action, data)
        val jsonEvent = Json.encodeToString(serializer, event)

        sendEventToUser(
            userId,
            jsonEvent
        )
    }

    suspend fun <T> broadcastEvent(event: WebSocketEvent<T>) {
        mutex.withLock {
            connections.values.flatten().forEach { session ->
                session.sendSerialized(event)
            }
        }
    }

}