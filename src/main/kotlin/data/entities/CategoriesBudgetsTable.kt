package data.entities

import org.jetbrains.exposed.sql.Table

object CategoriesBudgetsTable : Table() {
    val categoryId = reference("category_id", CategoriesTable.id)
    val budgetId = reference("budget_id", BudgetsTable.id)

    override val primaryKey = PrimaryKey(categoryId, budgetId)
}