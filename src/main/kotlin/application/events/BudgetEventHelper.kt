package application.events

import application.websockets.WebSocketManager
import application.websockets.WebSocketConstants
import domain.modules.budgets.repositories.BudgetRepository
import domain.modules.budgets.services.BudgetSummaryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BudgetEventHelper(
    private val budgetRepository: BudgetRepository,
    private val budgetSummaryService: BudgetSummaryService,
    private val coroutineScope: CoroutineScope
) {
    suspend fun notifyBudgetsAffectedByCategory(categoryId: Int, userId: Int) {
        val budgetIds = budgetRepository.getBudgetsByCategory(categoryId)

        budgetIds.forEach { budgetId ->
            coroutineScope.launch {
                val summary = budgetSummaryService.getBudgetSummary(userId = userId, budgetId = budgetId).getOrNull()!!
                WebSocketManager.sendEvent(
                    userId = userId,
                    entityType = WebSocketConstants.EntityType.BUDGET_SUMMARY,
                    action = WebSocketConstants.Action.UPDATED,
                    data = summary
                )
            }
        }
    }
}
