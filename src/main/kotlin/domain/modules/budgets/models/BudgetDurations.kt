package domain.modules.budgets.models

enum class BudgetDurations(val days: Int) {
    WEEKLY(7),
    MONTHLY(30),
    YEARLY(365),
}