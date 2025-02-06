package domain.modules.budgets.services

import domain.exceptions.NotFoundException
import domain.modules.budgets.models.budgetCategories.BudgetCategoriesRequest
import domain.modules.budgets.models.budgetCategories.BudgetCategoriesResponse
import domain.modules.budgets.repositories.BudgetRepository
import domain.modules.categories.repositories.CategoryRepository
import domain.validation.validateAndProcess

class BudgetCategoriesService(private val budgetRepository: BudgetRepository, private val categoryRepository: CategoryRepository) {

    suspend fun getBudgetCategories(budgetId: Int, userId: Int): Result<BudgetCategoriesResponse> {
        if (budgetRepository.getBudgetById(budgetId, userId) == null) {
            return Result.failure(NotFoundException("Budget with id $budgetId not found"))
        }

        return Result.success(budgetRepository.getBudgetCategories(budgetId, userId))
    }

    suspend fun addBudgetCategories(budgetId: Int, userId: Int, request: BudgetCategoriesRequest): Result<BudgetCategoriesResponse> {
        return request.validateAndProcess {
            if (budgetRepository.getBudgetById(budgetId = budgetId, userId = userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Budget with id $budgetId not found"))
            }

            val notFoundIds = categoryRepository.checkCategoriesByIds(request.categories, userId)
            if (notFoundIds.isNotEmpty()) {
                return@validateAndProcess Result.failure(NotFoundException("Following IDs not found: ${notFoundIds.joinToString()}"))
            }

            val updatedBudget = budgetRepository.addBudgetCategories(budgetId = budgetId, categoriesIds = request)
            Result.success(updatedBudget)
        }
    }

    suspend fun removeBudgetCategories(budgetId: Int, userId: Int, request: BudgetCategoriesRequest): Result<Boolean> {
        return request.validateAndProcess {
            if (budgetRepository.getBudgetById(budgetId = budgetId, userId = userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Budget with id $budgetId not found"))
            }

            Result.success(budgetRepository.removeBudgetCategories(budgetId, request))
        }
    }
}