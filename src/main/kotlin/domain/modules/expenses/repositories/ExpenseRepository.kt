package domain.modules.expenses.repositories

import data.database.DbManager.dbQuery
import data.entities.ExpensesTable
import domain.modules.expenses.models.ExpenseFilter
import domain.modules.expenses.models.CreateExpenseRequest
import domain.modules.expenses.models.ExpenseResponse
import domain.modules.expenses.models.UpdateExpenseRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ExpenseRepository {

    suspend fun getExpenses(filter: ExpenseFilter): List<ExpenseResponse> = dbQuery {
        var query: Query = ExpensesTable.selectAll().where { ExpensesTable.userId eq filter.userId }

        filter.categoryId?.let { query = query.andWhere { ExpensesTable.categoryId eq it } }
        filter.categoriesIds?.let { query = query.andWhere { ExpensesTable.categoryId inList it } }
        filter.minAmount?.let { query = query.andWhere { ExpensesTable.amount greaterEq it } }
        filter.maxAmount?.let { query = query.andWhere { ExpensesTable.amount lessEq it } }
        filter.startDate?.let { query = query.andWhere { ExpensesTable.date greaterEq it } }
        filter.endDate?.let { query = query.andWhere { ExpensesTable.date lessEq it } }

        val orderColumn = when (filter.orderBy) {
            "amount" -> ExpensesTable.amount
            "date" -> ExpensesTable.date
            "category" -> ExpensesTable.categoryId
            else -> ExpensesTable.date
        }

        val sortOrder = if (filter.orderDirection?.uppercase() == "ASC") SortOrder.ASC else SortOrder.DESC
        query = query.orderBy(orderColumn, sortOrder)

        query = query.limit(filter.pageSize).offset(start = ((filter.page - 1) * filter.pageSize).toLong())

        query.map {
            it.toExpenseResponse()
        }
    }

    suspend fun getExpenseById(expenseId: Int, userId: Int): ExpenseResponse? = dbQuery {
        ExpensesTable.selectAll()
            .where { (ExpensesTable.id eq expenseId) and (ExpensesTable.userId eq userId) }
            .map { it.toExpenseResponse() }
            .singleOrNull()
    }

    suspend fun createExpense(user: Int, expense: CreateExpenseRequest) = dbQuery {
        val id = ExpensesTable.insert {
            it[reason] = expense.reason
            it[amount] = expense.amount
            it[date] = LocalDateTime.now()
            it[userId] = user
            it[categoryId] = expense.categoryId
        } get ExpensesTable.id

        ExpensesTable.selectAll()
            .where { ExpensesTable.id eq id}
            .map { it.toExpenseResponse() }
            .single()
    }

    suspend fun updateExpense(expenseId: Int, userId: Int, expense: UpdateExpenseRequest): ExpenseResponse = dbQuery {
        ExpensesTable.update({ (ExpensesTable.id eq expenseId) and (ExpensesTable.userId eq userId) }) { row ->
            expense.reason?.let { row[reason] = expense.reason }
            expense.amount?.let { row[amount] = expense.amount }
            expense.categoryId?.let { row[categoryId] = expenseId }
        }

        ExpensesTable.selectAll()
            .where{ ExpensesTable.id eq expenseId }
            .map { it.toExpenseResponse() }
            .single()
    }

    suspend fun deleteExpense(expenseId: Int, userId: Int) = dbQuery {
        ExpensesTable.deleteWhere { (ExpensesTable.id eq expenseId) and (ExpensesTable.userId eq userId) } > 0
    }

    private fun ResultRow.toExpenseResponse() = ExpenseResponse(
        id = this[ExpensesTable.id],
        reason = this[ExpensesTable.reason],
        amount = this[ExpensesTable.amount],
        date = this[ExpensesTable.date].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        category = this[ExpensesTable.categoryId],
        userId = this[ExpensesTable.userId],
    )
}