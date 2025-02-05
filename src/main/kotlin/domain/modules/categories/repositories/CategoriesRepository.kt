package domain.modules.categories.repositories

import data.database.DbManager.dbQuery
import data.entities.CategoriesBudgetsTable
import data.entities.CategoriesTable
import data.extensions.andIfNotNull
import data.extensions.queryEqOptional
import domain.modules.categories.models.CategoryBudgetsResponse
import domain.modules.categories.models.CategoryResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CategoriesRepository {

    suspend fun getAllCategories(userId: Int?): List<CategoryResponse> = dbQuery {
        CategoriesTable.selectAll()
            .queryEqOptional(CategoriesTable.userId, userId)
            .map { it.toCategoryResponse() }
    }

    suspend fun getCategoryById(categoryId: Int, userId: Int?): CategoryResponse? = dbQuery {
        CategoriesTable.selectAll()
            .where { (CategoriesTable.id eq categoryId).andIfNotNull(userId) { CategoriesTable.userId eq it } }
            .map { it.toCategoryResponse() }
            .singleOrNull()
    }

    suspend fun checkCategoriesByIds(categoriesIds: Set<Int>, userId: Int): Set<Int> = dbQuery {
        val categories = CategoriesTable.selectAll()
            .where { (CategoriesTable.id inList categoriesIds) }
            .map { it[CategoriesTable.id] }.toSet()

        categoriesIds.subtract(categories)
    }

    suspend fun getCategoryByName(userId: Int, name: String, categoryId: Int? = null): CategoryResponse? = dbQuery {
        CategoriesTable.selectAll()
            .where {
                (CategoriesTable.name eq name) and (CategoriesTable.userId eq userId)
                    .andIfNotNull(categoryId) { CategoriesTable.id neq it }
            }
            .map { it.toCategoryResponse() }
            .singleOrNull()
    }

    suspend fun createCategory(name: String, userId: Int): CategoryResponse = dbQuery {
        val id = CategoriesTable.insert {
            it[CategoriesTable.name] = name
            it[CategoriesTable.userId] = userId
        } get CategoriesTable.id

        CategoriesTable.selectAll()
            .where { CategoriesTable.id eq id}
            .map { it.toCategoryResponse() }
            .single()
    }

    suspend fun updateCategory(id: Int, name: String?): CategoryResponse = dbQuery {
        CategoriesTable.update({ CategoriesTable.id eq id}) {
            if (name != null) it[CategoriesTable.name] = name
        }

        CategoriesTable.selectAll()
            .where { CategoriesTable.id eq id}
            .map { it.toCategoryResponse() }
            .single()
    }

    suspend fun deleteCategory(id: Int, userId: Int): Boolean = dbQuery {
        CategoriesTable.deleteWhere { (CategoriesTable.id eq id) and (CategoriesTable.userId eq userId) } > 0
    }

    suspend fun getCategoryBudgets(categoryId: Int): CategoryBudgetsResponse = dbQuery {
        val ids = (CategoriesBudgetsTable innerJoin CategoriesTable)
            .select(CategoriesBudgetsTable.budgetId)
            .where { (CategoriesBudgetsTable.categoryId eq categoryId) }
            .map { it[CategoriesBudgetsTable.budgetId] }.toSet()

        CategoryBudgetsResponse(ids)
    }

    private fun ResultRow.toCategoryResponse() = CategoryResponse(
        id = this[CategoriesTable.id],
        name = this[CategoriesTable.name],
    )
}