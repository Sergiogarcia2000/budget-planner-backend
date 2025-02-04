package domain.modules.budgets.models

import kotlinx.serialization.Serializable

@Serializable
data class BudgetResponse(
    val id: Int,
    val name: String,
    val limit: Int,
    val duration: Int,
    val startDate: String,
    val finished: Boolean,
    val recurrent: Boolean,
)
