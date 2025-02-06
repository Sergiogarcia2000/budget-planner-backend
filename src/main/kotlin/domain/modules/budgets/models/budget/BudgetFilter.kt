package domain.modules.budgets.models.budget

import java.time.LocalDate

data class BudgetFilter(
    val userId: Int,
    val minLimit: Int? = null,
    val maxLimit: Int? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val recurrent: Boolean? = null,
    val finished: Boolean? = null,
    val orderBy: String? = "startDate",
    val orderDirection: String? = "DESC",
    val page: Int = 1,
    val pageSize: Int = 10
)
