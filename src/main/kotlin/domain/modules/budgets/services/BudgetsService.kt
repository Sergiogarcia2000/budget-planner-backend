package domain.modules.budgets.services

import application.websockets.WebSocketConstants
import application.websockets.WebSocketManager
import domain.exceptions.NotFoundException
import domain.modules.budgets.models.*
import domain.modules.budgets.repositories.BudgetsRepository
import domain.validation.validateAndProcess

class BudgetsService(private val budgetsRepository: BudgetsRepository) {

    suspend fun getAllBudgets(filter: BudgetFilter): List<BudgetResponse> = budgetsRepository.getAllBudgets(filter)

    suspend fun getBudget(budgetId: Int, userId: Int): BudgetResponse? = budgetsRepository.getBudgetById(budgetId, userId)

    suspend fun create(userId: Int, request: CreateBudgetRequest): Result<BudgetResponse> {
        return request.validateAndProcess { body ->
            val budget = budgetsRepository.createBudget(userId, body)

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
            if (budgetsRepository.getBudgetById(budgetId, userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Budget with id $budgetId not found"))
            }

            val updated = budgetsRepository.updateBudget(budgetId, userId, body)

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
        if (budgetsRepository.getBudgetById(budgetId, userId) == null) {
            return Result.failure(NotFoundException("Budget with id $budgetId not found"))
        }

        val deleted = budgetsRepository.deleteBudget(budgetId, userId)

        WebSocketManager.sendEvent(
            userId = userId,
            entityType = WebSocketConstants.EntityType.BUDGET,
            action = WebSocketConstants.Action.DELETED,
            data = budgetId
        )

        return Result.success(deleted)
    }

    suspend fun getBudgetCategories(budgetId: Int, userId: Int): Result<BudgetCategoriesResponse> {
        if (budgetsRepository.getBudgetById(budgetId, userId) == null) {
            return Result.failure(NotFoundException("Budget with id $budgetId not found"))
        }

        return Result.success(budgetsRepository.getBudgetCategories(budgetId, userId))
    }

    suspend fun addBudgetCategories(budgetId: Int, userId: Int, request: BudgetCategoriesRequest): Result<BudgetCategoriesResponse> {
        return request.validateAndProcess {
            if (budgetsRepository.getBudgetById(budgetId = budgetId, userId = userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Budget with id $budgetId not found"))
            }

            val updatedBudget = budgetsRepository.addBudgetCategories(budgetId = budgetId, categoriesIds = request)
            Result.success(updatedBudget)
        }
    }

    suspend fun removeBudgetCategories(budgetId: Int, userId: Int, request: BudgetCategoriesRequest): Result<Boolean> {
        return request.validateAndProcess {
            if (budgetsRepository.getBudgetById(budgetId = budgetId, userId = userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Budget with id $budgetId not found"))
            }

            Result.success(budgetsRepository.removeBudgetCategories(budgetId, request))
        }
    }

}