package data.entities

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object ExpensesTable : Table() {
    val id = integer("id").autoIncrement()
    val reason = varchar("name", 255)
    val date = datetime("created_at").default(LocalDateTime.now())
    val amount = double("amount")
    val userId = reference("user_id", UsersTable.id)
    val categoryId = reference("category_id", CategoriesTable.id)

    override val primaryKey = PrimaryKey(id)
}