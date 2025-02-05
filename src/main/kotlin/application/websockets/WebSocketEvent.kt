package application.websockets

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketEvent<T>(
    val entityType: String,
    val action: String,
    val data: T
)