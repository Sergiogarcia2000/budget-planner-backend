package domain.modules.budgets.services

import domain.exceptions.NotFoundException
import domain.modules.budgets.models.budget.BudgetFilter
import domain.modules.budgets.models.budget.BudgetResponse
import domain.modules.budgets.models.budgetSummary.BudgetSummaryResponse
import domain.modules.budgets.repositories.BudgetRepository
import domain.modules.expenses.models.ExpenseFilter
import domain.modules.expenses.repositories.ExpenseRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class BudgetSummaryService(private val budgetRepository: BudgetRepository,private val expenseRepository: ExpenseRepository) {

    suspend fun getBudgetsSummaries(userId: Int): List<BudgetSummaryResponse > {
        val userBudgets = budgetRepository.getAllBudgets(BudgetFilter(userId = userId))
        return userBudgets.map { calculateBudgetSummary(it, userId) }
    }

    suspend fun getBudgetSummary(userId: Int, budgetId: Int): Result<BudgetSummaryResponse> {
        val budget = budgetRepository.getBudgetById(budgetId = budgetId, userId = userId)
            ?: return Result.failure(NotFoundException("Budget with id $budgetId does not exist."))

        return Result.success(calculateBudgetSummary(budget = budget, userId = userId))
    }

    private suspend fun calculateBudgetSummary(budget: BudgetResponse, userId: Int): BudgetSummaryResponse {

        val budgetCategories = budgetRepository.getBudgetCategories(budgetId = budget.id, userId = userId).categories

        val startPeriod = calculateStartPeriod(LocalDate.parse(budget.startDate), budget.duration)
        val remainingDays = calculateRemainingDays(startDate = startPeriod.toLocalDate(), budgetDuration = budget.duration)

        val expenses = expenseRepository.getExpenses(
            ExpenseFilter(
                userId = userId,
                categoriesIds = budgetCategories,
                startDate = startPeriod
            )
        )

        val totalSpent = expenses.sumOf { it.amount }
        val totalRemaining = budget.limit - totalSpent
        val exceeded = totalSpent > budget.limit

        return BudgetSummaryResponse(
            budgetId = budget.id,
            name = budget.name,
            startDate = budget.startDate,
            remainingDays = remainingDays,
            limit = budget.limit,
            totalSpent = totalSpent,
            totalRemaining = totalRemaining,
            exceeded = exceeded
        )
    }

    private fun calculateRemainingDays(startDate: LocalDate, budgetDuration: Int): Int {
        val endPeriodDate = startDate.plusDays(budgetDuration.toLong())
        return ChronoUnit.DAYS.between(LocalDate.now(), endPeriodDate).toInt()
    }

    private fun calculateStartPeriod(startDate: LocalDate, budgetDuration: Int): LocalDateTime {
        val currentDate = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(startDate, currentDate)
        val cycles = (daysBetween / budgetDuration).toInt()
        val daysUntilCycle = cycles * budgetDuration
        val startPeriod = startDate.plusDays(daysUntilCycle.toLong())

        return startPeriod.atStartOfDay()
    }
}