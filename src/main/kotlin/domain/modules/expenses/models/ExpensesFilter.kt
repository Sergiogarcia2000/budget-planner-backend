package domain.modules.expenses.models

import java.time.LocalDateTime

data class ExpenseFilter(
    val userId: Int,
    val categoryId: Int? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val orderBy: String? = "date",
    val orderDirection: String? = "DESC",
    val page: Int = 1,
    val pageSize: Int = 10
)