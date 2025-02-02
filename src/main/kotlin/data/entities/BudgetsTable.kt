package data.entities

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object BudgetsTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val limit = long("limit")
    val budgetDuration = datetime("duration")
    val budgetStartDate = datetime("start_date")
    val recurrent = bool("recurrent")
    val finished = bool("finished")
    val userId = reference("user_id", UsersTable.id)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}