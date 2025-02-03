package domain.modules.expenses.models

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseResponse(
    val id: Int,
    val reason: String,
    val amount: Double,
    val date: String,
    val category: Int,
    val userId: Int
)
