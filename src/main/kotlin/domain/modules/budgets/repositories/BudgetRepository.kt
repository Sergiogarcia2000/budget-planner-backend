package domain.modules.budgets.repositories

import data.database.DbManager.dbQuery
import data.entities.BudgetsTable
import data.entities.CategoriesBudgetsTable
import domain.modules.budgets.models.budget.BudgetFilter
import domain.modules.budgets.models.budget.BudgetResponse
import domain.modules.budgets.models.budget.CreateBudgetRequest
import domain.modules.budgets.models.budget.UpdateBudgetRequest
import domain.modules.budgets.models.budgetCategories.BudgetCategoriesRequest
import domain.modules.budgets.models.budgetCategories.BudgetCategoriesResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class BudgetRepository {
    suspend fun getAllBudgets(filter: BudgetFilter): List<BudgetResponse> = dbQuery {
        var query: Query = BudgetsTable.selectAll().where { BudgetsTable.userId eq filter.userId }

        filter.minLimit?.let { query = query.andWhere { BudgetsTable.limit greaterEq it } }
        filter.maxLimit?.let { query = query.andWhere { BudgetsTable.limit lessEq it } }
        filter.startDate?.let { query = query.andWhere { BudgetsTable.startDate greaterEq it } }
        filter.endDate?.let { query = query.andWhere { BudgetsTable.startDate lessEq it } }
        filter.recurrent?.let { query = query.andWhere { BudgetsTable.recurrent eq it } }
        filter.finished?.let { query = query.andWhere { BudgetsTable.finished eq it } }

        val orderColumn = when (filter.orderBy) {
            "name" -> BudgetsTable.name
            "limit" -> BudgetsTable.limit
            "duration" -> BudgetsTable.duration
            "startDate" -> BudgetsTable.startDate
            else -> BudgetsTable.startDate
        }

        val sortOrder = if (filter.orderDirection?.uppercase() == "ASC") SortOrder.ASC else SortOrder.DESC
        query = query.orderBy(orderColumn, sortOrder)

        query = query.limit(filter.pageSize).offset(start = ((filter.page - 1) * filter.pageSize).toLong())

        query.map {
            it.toBudgetResponse()
        }
    }

    suspend fun getBudgetById(budgetId: Int, userId: Int): BudgetResponse? = dbQuery {
        BudgetsTable.selectAll()
            .where { (BudgetsTable.id eq budgetId) and (BudgetsTable.userId eq userId) }
            .map { it.toBudgetResponse() }
            .singleOrNull()
    }

    suspend fun createBudget(user: Int, budget: CreateBudgetRequest) = dbQuery {
        val id = BudgetsTable.insert { row ->
            row[name] = budget.name
            row[limit] = budget.limit
            row[duration] = budget.duration
            row[startDate] = budget.startDate.toLocalDate()
            row[recurrent] = budget.recurrent
            row[userId] = user
        } get BudgetsTable.id

        BudgetsTable.selectAll()
            .where {BudgetsTable.id eq id }
            .map { it.toBudgetResponse() }
            .single()
    }

    suspend fun updateBudget(budgetId: Int, userId: Int, budget: UpdateBudgetRequest) = dbQuery {
        BudgetsTable.update({ (BudgetsTable.id eq budgetId) and (BudgetsTable.userId eq userId) }) { row ->
            budget.name?.let { row[name] = it }
            budget.limit?.let { row[limit] = it }
            budget.duration?.let { row[duration] = it }
            budget.startDate?.let { row[startDate] = it.toLocalDate() }
            budget.recurrent?.let { row[recurrent] = it }
            budget.finished?.let { row[finished] = it }
        }

        BudgetsTable.selectAll()
            .where {BudgetsTable.id eq budgetId }
            .map { it.toBudgetResponse() }
            .single()
    }

    suspend fun deleteBudget(budgetId: Int, userId: Int) = dbQuery {
        BudgetsTable.deleteWhere { (BudgetsTable.id eq budgetId) and (BudgetsTable.userId eq userId) } > 0
    }

    suspend fun getBudgetsByCategory(categoryId: Int): List<Int> = dbQuery {
        CategoriesBudgetsTable.select(CategoriesBudgetsTable.budgetId)
            .where { (CategoriesBudgetsTable.categoryId eq categoryId) }
            .map { it[CategoriesBudgetsTable.budgetId] }
    }

    suspend fun getBudgetCategories(budgetId: Int, userId: Int) = dbQuery {
        val ids = (CategoriesBudgetsTable innerJoin BudgetsTable)
            .select(CategoriesBudgetsTable.categoryId)
            .where { (CategoriesBudgetsTable.budgetId eq budgetId) and (BudgetsTable.userId eq userId) }
            .map { it[CategoriesBudgetsTable.categoryId] }.toSet()

        BudgetCategoriesResponse(ids)
    }

    suspend fun addBudgetCategories(budgetId: Int, categoriesIds: BudgetCategoriesRequest) = dbQuery {
        CategoriesBudgetsTable.batchInsert(categoriesIds.categories) { categoryId ->
            this[CategoriesBudgetsTable.budgetId] = budgetId
            this[CategoriesBudgetsTable.categoryId] = categoryId
        }.map { it[CategoriesBudgetsTable.categoryId] }.toSet()

        val ids = (CategoriesBudgetsTable innerJoin BudgetsTable)
            .select(CategoriesBudgetsTable.categoryId)
            .where { (CategoriesBudgetsTable.budgetId eq budgetId) }
            .map { it[CategoriesBudgetsTable.categoryId] }.toSet()

        BudgetCategoriesResponse(ids)
    }

    suspend fun removeBudgetCategories(budgetId: Int, categoriesId: BudgetCategoriesRequest) = dbQuery {
        CategoriesBudgetsTable.deleteWhere {
            (CategoriesBudgetsTable.budgetId eq budgetId) and
            (CategoriesBudgetsTable.categoryId inList categoriesId.categories)
        } > 0
    }

    private fun ResultRow.toBudgetResponse() = BudgetResponse(
        id = this[BudgetsTable.id],
        name = this[BudgetsTable.name],
        limit = this[BudgetsTable.limit],
        duration = this[BudgetsTable.duration],
        startDate = this[BudgetsTable.startDate].format(DateTimeFormatter.ISO_LOCAL_DATE),
        recurrent = this[BudgetsTable.recurrent],
        finished = this[BudgetsTable.finished]
    )

    private fun Date.toLocalDate(): LocalDate = this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}