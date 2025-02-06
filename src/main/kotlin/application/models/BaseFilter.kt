package application.models

import java.time.LocalDateTime

data class BaseFilter (
    val orderBy: String?,
    val orderDirection: String?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val page: Int,
    val pageSize: Int
)