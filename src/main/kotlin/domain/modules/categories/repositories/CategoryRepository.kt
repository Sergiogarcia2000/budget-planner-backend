package domain.modules.categories.repositories

import data.database.DbManager.dbQuery
import data.entities.CategoriesTable
import data.entities.UsersTable
import data.extensions.queryEqOptional
import data.extensions.queryNeqOptional
import domain.modules.categories.models.CategoryResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CategoryRepository {

    suspend fun getAllCategories(userId: Int?): List<CategoryResponse> = dbQuery {
        CategoriesTable.selectAll()
            .queryEqOptional(CategoriesTable.userId, userId)
            .map { toCategoryResponse(it) }
    }

    suspend fun getCategoryById(id: Int, userId: Int?): CategoryResponse? = dbQuery {
        CategoriesTable.selectAll()
            .where { CategoriesTable.id eq id }
            .queryEqOptional(CategoriesTable.userId, userId)
            .map { toCategoryResponse(it) }
            .singleOrNull()
    }

    suspend fun getCategoryByName(userId: Int, name: String, categoryId: Int? = null): CategoryResponse? = dbQuery {
        CategoriesTable.selectAll()
            .where {(CategoriesTable.name eq name) and (CategoriesTable.userId eq userId)}
            .queryNeqOptional(CategoriesTable.id, categoryId)
            .map { toCategoryResponse(it) }
            .singleOrNull()
    }

    suspend fun createCategory(name: String, userId: Int): CategoryResponse = dbQuery {
        val id = CategoriesTable.insert {
            it[CategoriesTable.name] = name
            it[CategoriesTable.userId] = userId
        } get CategoriesTable.id

        CategoriesTable.selectAll()
            .where { CategoriesTable.id eq id}
            .map { toCategoryResponse(it) }
            .single()
    }

    suspend fun updateCategory(id: Int, name: String?): CategoryResponse = dbQuery {
        CategoriesTable.update({ CategoriesTable.id eq id}) {
            if (name != null) it[CategoriesTable.name] = name
        }

        UsersTable.selectAll()
            .where { CategoriesTable.id eq id}
            .map { toCategoryResponse(it) }
            .single()
    }

    suspend fun deleteCategory(id: Int, userId: Int): Boolean = dbQuery {
        CategoriesTable.deleteWhere { (CategoriesTable.id eq id) and (CategoriesTable.userId eq userId) } > 0
    }

    private fun toCategoryResponse(row: ResultRow) = CategoryResponse(
        id = row[CategoriesTable.id],
        name = row[CategoriesTable.name],
    )
}