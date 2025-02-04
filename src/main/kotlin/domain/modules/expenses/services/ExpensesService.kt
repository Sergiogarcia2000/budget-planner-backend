package domain.modules.expenses.services

import domain.exceptions.NotFoundException
import domain.modules.expenses.models.CreateExpenseRequest
import domain.modules.expenses.models.ExpenseFilter
import domain.modules.expenses.models.ExpenseResponse
import domain.modules.expenses.models.UpdateExpenseRequest
import domain.modules.expenses.repositories.ExpensesRepository
import domain.validation.validateAndProcess

class ExpensesService(private val expensesRepository: ExpensesRepository) {

    suspend fun getAllExpenses(filter: ExpenseFilter): List<ExpenseResponse> = expensesRepository.getExpenses(filter)

    suspend fun getExpense(expenseId: Int, userId: Int): ExpenseResponse? = expensesRepository.getExpenseById(expenseId, userId)

    suspend fun create(userId: Int, request: CreateExpenseRequest): Result<ExpenseResponse> {
        return request.validateAndProcess { body ->
            val expense = expensesRepository.createExpense(userId, body)
            Result.success(expense)
        }
    }

    suspend fun update(userId: Int, expenseId: Int, request: UpdateExpenseRequest): Result<Boolean> {
        return request.validateAndProcess { body ->
            if (expensesRepository.getExpenseById(expenseId, userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Expense with id $expenseId not found"))
            }

            val updated = expensesRepository.updateExpense(expenseId, userId, body)
            Result.success(updated)
        }
    }

    suspend fun delete(userId: Int, expenseId: Int): Result<Boolean> {
        if (expensesRepository.getExpenseById(expenseId, userId) == null) {
            return Result.failure(NotFoundException("Expense with id $expenseId not found"))
        }
        return Result.success(expensesRepository.deleteExpense(expenseId, userId))
    }
}