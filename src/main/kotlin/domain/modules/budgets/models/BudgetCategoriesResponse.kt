package domain.modules.budgets.models

import kotlinx.serialization.Serializable

@Serializable
data class BudgetCategoriesResponse(
    val categories: Set<Int>
)