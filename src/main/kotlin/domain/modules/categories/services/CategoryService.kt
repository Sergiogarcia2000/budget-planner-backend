package domain.modules.categories.services

import domain.exceptions.NotFoundException
import domain.modules.categories.models.CategoryBudgetsResponse
import domain.modules.categories.models.CategoryRequest
import domain.modules.categories.models.CategoryResponse
import domain.modules.categories.repositories.CategoryRepository
import domain.validation.validateAndProcess
import java.lang.IllegalArgumentException

class CategoryService(private val categoryRepository: CategoryRepository) {

    suspend fun getAllCategories(userId: Int?): List<CategoryResponse> = categoryRepository.getAllCategories(userId)

    suspend fun getCategoryById(id: Int, userId: Int?): CategoryResponse? =
        categoryRepository.getCategoryById(id, userId)

    suspend fun create(userId: Int, request: CategoryRequest): Result<CategoryResponse> {
        return request.validateAndProcess { body ->

            if (categoryRepository.getCategoryByName(userId, body.name) != null) {
                return@validateAndProcess Result.failure(IllegalArgumentException("Category ${body.name} already exists."))
            }

            val category = categoryRepository.createCategory(body.name, userId)
            Result.success(category)
        }
    }

    suspend fun updateCategory(userId: Int, categoryId: Int, request: CategoryRequest): Result<CategoryResponse> {
        return request.validateAndProcess { body ->
            if (categoryRepository.getCategoryById(categoryId, userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Category $categoryId not found."))
            }

            if (categoryRepository.getCategoryByName(userId, body.name, categoryId) != null) {
                return@validateAndProcess Result.failure(IllegalArgumentException("Category ${body.name} already exists."))
            }

            val categoryUpdated = categoryRepository.updateCategory(categoryId, body.name)
            Result.success(categoryUpdated)
        }
    }

    suspend fun deleteCategory(id: Int, userId: Int): Result<Boolean> {
        if (categoryRepository.getCategoryById(id, userId) == null) {
            return Result.failure(NotFoundException("Category $id not found."))
        }

        return Result.success(categoryRepository.deleteCategory(id, userId))
    }

    suspend fun getCategoryBudgets(categoryId: Int, userId: Int): Result<CategoryBudgetsResponse> {
        if (categoryRepository.getCategoryById(categoryId, userId) == null) {
            return Result.failure(NotFoundException("Category $categoryId not found."))
        }

        return Result.success(categoryRepository.getCategoryBudgets(categoryId))
    }
}