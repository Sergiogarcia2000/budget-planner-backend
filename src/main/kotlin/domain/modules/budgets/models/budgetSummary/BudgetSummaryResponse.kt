package domain.modules.budgets.models.budgetSummary

import kotlinx.serialization.Serializable

@Serializable
data class BudgetSummaryResponse(
    val budgetId: Int,
    val name: String,
    val startDate: String,
    val remainingDays: Int,
    val limit: Int,
    val totalSpent: Double,
    val totalRemaining: Double,
    val exceeded: Boolean
)
