package domain.modules.categories.services

import application.websockets.WebSocketConstants
import application.websockets.WebSocketManager
import domain.exceptions.NotFoundException
import domain.modules.categories.models.CategoryBudgetsResponse
import domain.modules.categories.models.CategoryRequest
import domain.modules.categories.models.CategoryResponse
import domain.modules.categories.repositories.CategoriesRepository
import domain.validation.validateAndProcess
import java.lang.IllegalArgumentException

class CategoryService(private val categoriesRepository: CategoriesRepository) {

    suspend fun getAllCategories(userId: Int?): List<CategoryResponse> = categoriesRepository.getAllCategories(userId)

    suspend fun getCategoryById(categoryId: Int, userId: Int?): CategoryResponse? =
        categoriesRepository.getCategoryById(categoryId, userId)

    suspend fun create(userId: Int, request: CategoryRequest): Result<CategoryResponse> {
        return request.validateAndProcess { body ->

            if (categoriesRepository.getCategoryByName(userId, body.name) != null) {
                return@validateAndProcess Result.failure(IllegalArgumentException("Category ${body.name} already exists."))
            }

            val category = categoriesRepository.createCategory(body.name, userId)

            WebSocketManager.sendEvent(
                userId = userId,
                entityType = WebSocketConstants.EntityType.CATEGORY,
                action = WebSocketConstants.Action.CREATED,
                data = category
            )

            Result.success(category)
        }
    }

    suspend fun updateCategory(userId: Int, categoryId: Int, request: CategoryRequest): Result<CategoryResponse> {
        return request.validateAndProcess { body ->
            if (categoriesRepository.getCategoryById(categoryId, userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Category $categoryId not found."))
            }

            if (categoriesRepository.getCategoryByName(userId, body.name, categoryId) != null) {
                return@validateAndProcess Result.failure(IllegalArgumentException("Category ${body.name} already exists."))
            }

            val categoryUpdated = categoriesRepository.updateCategory(categoryId, body.name)

            WebSocketManager.sendEvent(
                userId = userId,
                entityType = WebSocketConstants.EntityType.CATEGORY,
                action = WebSocketConstants.Action.CREATED,
                data = categoryUpdated
            )

            Result.success(categoryUpdated)
        }
    }

    suspend fun deleteCategory(categoryId: Int, userId: Int): Result<Boolean> {
        if (categoriesRepository.getCategoryById(categoryId, userId) == null) {
            return Result.failure(NotFoundException("Category $categoryId not found."))
        }

        val deleted = categoriesRepository.deleteCategory(categoryId, userId)

        WebSocketManager.sendEvent(
            userId = userId,
            entityType = WebSocketConstants.EntityType.CATEGORY,
            action = WebSocketConstants.Action.CREATED,
            data = categoryId
        )

        return Result.success(deleted)
    }

    suspend fun getCategoryBudgets(categoryId: Int, userId: Int): Result<CategoryBudgetsResponse> {
        if (categoriesRepository.getCategoryById(categoryId, userId) == null) {
            return Result.failure(NotFoundException("Category $categoryId not found."))
        }

        return Result.success(categoriesRepository.getCategoryBudgets(categoryId))
    }
}