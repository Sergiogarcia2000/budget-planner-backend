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

class ExpensesRepository {

    suspend fun getExpenses(filter: ExpenseFilter): List<ExpenseResponse> = dbQuery {
        // 🔹 Construir la query de manera dinámica
        var query: Query = ExpensesTable.selectAll().where { ExpensesTable.userId eq filter.userId }

        // 🔹 Aplicar filtros opcionales
        filter.categoryId?.let { query = query.andWhere { ExpensesTable.categoryId eq it } }
        filter.minAmount?.let { query = query.andWhere { ExpensesTable.amount greaterEq it } }
        filter.maxAmount?.let { query = query.andWhere { ExpensesTable.amount lessEq it } }
        filter.startDate?.let { query = query.andWhere { ExpensesTable.date greaterEq it } }
        filter.endDate?.let { query = query.andWhere { ExpensesTable.date lessEq it } }

        // 🔹 Aplicar ordenación
        val orderColumn = when (filter.orderBy) {
            "amount" -> ExpensesTable.amount
            "date" -> ExpensesTable.date
            "category" -> ExpensesTable.categoryId
            else -> ExpensesTable.date // 🔹 Default: ordenar por fecha
        }

        val sortOrder = if (filter.orderDirection?.uppercase() == "ASC") SortOrder.ASC else SortOrder.DESC
        query = query.orderBy(orderColumn, sortOrder)

        // 🔹 Aplicar paginación
        query = query.limit(filter.pageSize).offset(start = ((filter.page - 1) * filter.pageSize).toLong())

        // 🔹 Ejecutar la query y mapear resultados
        query.map {
            it.toExpenseResponse()
        }
    }

    suspend fun getExpenseById(id: Int, userId: Int): ExpenseResponse? = dbQuery {
        ExpensesTable.selectAll()
            .where { (ExpensesTable.id eq id) and (ExpensesTable.userId eq userId) }
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

    suspend fun updateExpense(id: Int, userId: Int, expense: UpdateExpenseRequest): Boolean = dbQuery {
        ExpensesTable.update({ (ExpensesTable.id eq id) and (ExpensesTable.userId eq userId) }) { row ->
            expense.reason?.let { row[reason] = expense.reason }
            expense.amount?.let { row[amount] = expense.amount }
            expense.categoryId?.let { row[categoryId] = id }
        } > 0
    }

    suspend fun deleteExpense(id: Int, userId: Int) = dbQuery {
        ExpensesTable.deleteWhere { (ExpensesTable.id eq id) and (ExpensesTable.userId eq userId) } > 0
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