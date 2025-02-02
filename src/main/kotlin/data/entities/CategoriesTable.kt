package data.entities

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object CategoriesTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val userId = reference("user_id", UsersTable.id)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}