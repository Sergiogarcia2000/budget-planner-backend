package application.websockets

object WebSocketConstants {
    object EntityType {
        const val EXPENSE = "EXPENSE"
        const val BUDGET = "BUDGET"
        const val CATEGORY = "CATEGORY"
    }

    object Action {
        const val CREATED = "CREATED"
        const val UPDATED = "UPDATED"
        const val DELETED = "DELETED"
    }
}