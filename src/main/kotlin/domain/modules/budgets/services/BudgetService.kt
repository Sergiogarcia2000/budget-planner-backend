package domain.modules.budgets.services

import application.websockets.WebSocketConstants
import application.websockets.WebSocketManager
import domain.exceptions.NotFoundException
import domain.modules.budgets.models.budget.BudgetFilter
import domain.modules.budgets.models.budget.BudgetResponse
import domain.modules.budgets.models.budget.CreateBudgetRequest
import domain.modules.budgets.models.budget.UpdateBudgetRequest
import domain.modules.budgets.repositories.BudgetRepository
import domain.validation.validateAndProcess

class BudgetService(private val budgetRepository: BudgetRepository) {

    suspend fun getAllBudgets(filter: BudgetFilter): List<BudgetResponse> = budgetRepository.getAllBudgets(filter)

    suspend fun getBudget(budgetId: Int, userId: Int): Result<BudgetResponse> {
        val budget = budgetRepository.getBudgetById(budgetId, userId)
            ?: return Result.failure(NotFoundException("Budget with ID $budgetId not found"))

        return Result.success(budget)
    }

    suspend fun create(userId: Int, request: CreateBudgetRequest): Result<BudgetResponse> {
        return request.validateAndProcess { body ->
            val budget = budgetRepository.createBudget(userId, body)

            WebSocketManager.sendEvent(
                userId = userId,
                entityType = WebSocketConstants.EntityType.BUDGET,
                action = WebSocketConstants.Action.CREATED,
                data = budget
            )

            Result.success(budget)
        }
    }

    suspend fun update(budgetId: Int, userId: Int, request: UpdateBudgetRequest): Result<BudgetResponse> {
        return request.validateAndProcess { body ->
            if (budgetRepository.getBudgetById(budgetId, userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Budget with id $budgetId not found"))
            }

            val updated = budgetRepository.updateBudget(budgetId, userId, body)

            WebSocketManager.sendEvent(
                userId = userId,
                entityType = WebSocketConstants.EntityType.BUDGET,
                action = WebSocketConstants.Action.UPDATED,
                data = updated
            )

            Result.success(updated)
        }
    }

    suspend fun delete(budgetId: Int, userId: Int): Result<Boolean> {
        if (budgetRepository.getBudgetById(budgetId, userId) == null) {
            return Result.failure(NotFoundException("Budget with id $budgetId not found"))
        }

        val deleted = budgetRepository.deleteBudget(budgetId, userId)

        WebSocketManager.sendEvent(
            userId = userId,
            entityType = WebSocketConstants.EntityType.BUDGET,
            action = WebSocketConstants.Action.DELETED,
            data = budgetId
        )

        return Result.success(deleted)
    }
}