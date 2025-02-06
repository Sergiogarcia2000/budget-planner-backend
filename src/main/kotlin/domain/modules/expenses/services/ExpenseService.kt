package domain.modules.expenses.services

import application.websockets.WebSocketConstants
import application.websockets.WebSocketManager
import domain.exceptions.NotFoundException
import domain.modules.categories.repositories.CategoryRepository
import domain.modules.expenses.models.CreateExpenseRequest
import domain.modules.expenses.models.ExpenseFilter
import domain.modules.expenses.models.ExpenseResponse
import domain.modules.expenses.models.UpdateExpenseRequest
import domain.modules.expenses.repositories.ExpenseRepository
import domain.validation.validateAndProcess

class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend fun getAllExpenses(filter: ExpenseFilter): List<ExpenseResponse> = expenseRepository.getExpenses(filter)

    suspend fun getExpense(expenseId: Int, userId: Int): ExpenseResponse? = expenseRepository.getExpenseById(expenseId, userId)

    suspend fun create(userId: Int, request: CreateExpenseRequest): Result<ExpenseResponse> {
        return request.validateAndProcess { body ->

            if (categoryRepository.getCategoryById(categoryId = body.categoryId, userId = userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Category ${body.categoryId} not found"))
            }

            val expense = expenseRepository.createExpense(userId, body)
            WebSocketManager.sendEvent(
                userId = userId,
                entityType = WebSocketConstants.EntityType.EXPENSE,
                action = WebSocketConstants.Action.CREATED,
                data = expense
            )

            Result.success(expense)
        }
    }

    suspend fun update(userId: Int, expenseId: Int, request: UpdateExpenseRequest): Result<ExpenseResponse> {
        return request.validateAndProcess { body ->
            if (expenseRepository.getExpenseById(expenseId, userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Expense with id $expenseId not found"))
            }

            if (body.categoryId != null && categoryRepository.getCategoryById(categoryId = body.categoryId, userId = userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Category ${body.categoryId} not found"))
            }

            val updated = expenseRepository.updateExpense(expenseId, userId, body)

            WebSocketManager.sendEvent(
                userId = userId,
                entityType = WebSocketConstants.EntityType.EXPENSE,
                action = WebSocketConstants.Action.UPDATED,
                data = updated
            )

            Result.success(updated)
        }
    }

    suspend fun delete(userId: Int, expenseId: Int): Result<Boolean> {
        if (expenseRepository.getExpenseById(expenseId, userId) == null) {
            return Result.failure(NotFoundException("Expense with id $expenseId not found"))
        }

        WebSocketManager.sendEvent(
            userId = userId,
            entityType = WebSocketConstants.EntityType.EXPENSE,
            action = WebSocketConstants.Action.DELETED,
            data = expenseId
        )

        return Result.success(expenseRepository.deleteExpense(expenseId, userId))
    }
}