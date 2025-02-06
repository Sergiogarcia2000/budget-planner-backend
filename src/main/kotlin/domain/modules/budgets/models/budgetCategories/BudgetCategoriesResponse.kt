package domain.modules.budgets.models.budgetCategories

import kotlinx.serialization.Serializable

@Serializable
data class BudgetCategoriesResponse(
    val categories: Set<Int>
)