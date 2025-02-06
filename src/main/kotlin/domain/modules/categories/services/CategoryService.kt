package domain.modules.categories.services

import application.models.BaseFilter
import application.websockets.WebSocketConstants
import application.websockets.WebSocketManager
import domain.exceptions.NotFoundException
import domain.modules.categories.models.CategoryBudgetsResponse
import domain.modules.categories.models.CategoryRequest
import domain.modules.categories.models.CategoryResponse
import domain.modules.categories.repositories.CategoryRepository
import domain.validation.validateAndProcess
import java.lang.IllegalArgumentException

class CategoryService(private val categoryRepository: CategoryRepository) {

    suspend fun getAllCategories(userId: Int?, filter: BaseFilter): List<CategoryResponse> = categoryRepository.getAllCategories(userId, filter)

    suspend fun getCategoryById(categoryId: Int, userId: Int?): Result<CategoryResponse> {
        val category =  categoryRepository.getCategoryById(categoryId, userId)
            ?: return Result.failure(NotFoundException("Category with ID $categoryId not found"))

        return Result.success(category)
    }

    suspend fun create(userId: Int, request: CategoryRequest): Result<CategoryResponse> {
        return request.validateAndProcess { body ->

            if (categoryRepository.getCategoryByName(userId, body.name) != null) {
                return@validateAndProcess Result.failure(IllegalArgumentException("Category ${body.name} already exists."))
            }

            val category = categoryRepository.createCategory(body.name, userId)

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
            if (categoryRepository.getCategoryById(categoryId, userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("Category $categoryId not found."))
            }

            if (categoryRepository.getCategoryByName(userId, body.name, categoryId) != null) {
                return@validateAndProcess Result.failure(IllegalArgumentException("Category ${body.name} already exists."))
            }

            val categoryUpdated = categoryRepository.updateCategory(categoryId, body.name)

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
        if (categoryRepository.getCategoryById(categoryId, userId) == null) {
            return Result.failure(NotFoundException("Category $categoryId not found."))
        }

        val deleted = categoryRepository.deleteCategory(categoryId, userId)

        WebSocketManager.sendEvent(
            userId = userId,
            entityType = WebSocketConstants.EntityType.CATEGORY,
            action = WebSocketConstants.Action.CREATED,
            data = categoryId
        )

        return Result.success(deleted)
    }

    suspend fun getCategoryBudgets(categoryId: Int, userId: Int): Result<CategoryBudgetsResponse> {
        if (categoryRepository.getCategoryById(categoryId, userId) == null) {
            return Result.failure(NotFoundException("Category $categoryId not found."))
        }

        return Result.success(categoryRepository.getCategoryBudgets(categoryId))
    }
}