package domain.modules.users.repositories

import data.entities.UsersTable
import data.database.DbManager.dbQuery
import domain.modules.users.models.UserResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class UserRepository {

    suspend fun getAllUsers(): List<UserResponse> = dbQuery {
        UsersTable.selectAll().map { toUserResponse(it) }
    }

    suspend fun getUserById(id: Int): UserResponse? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.id eq id}
            .map { toUserResponse(it) }
            .singleOrNull()
    }

    suspend fun getUserByEmail(email: String): UserResponse? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.email eq email}
            .map { toUserResponse(it) }
            .singleOrNull()
    }

    suspend fun createUser(name: String, email: String, hashedPassword: String): UserResponse = dbQuery {
        val id = UsersTable.insert {
            it[UsersTable.name] = name
            it[UsersTable.email] = email
            it[UsersTable.hashedPassword] = hashedPassword
            it[UsersTable.createdAt] = LocalDateTime.now()
        } get UsersTable.id

        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .map { toUserResponse(it) }
            .single()
    }

    suspend fun deleteUser(userId: Int) = dbQuery {
        UsersTable.deleteWhere { UsersTable.id eq userId } > 0
    }

    suspend fun updateUser(id: Int, name: String?, email: String?, hashedPassword: String?): UserResponse = dbQuery {
        UsersTable.update({ UsersTable.id eq id }) {
            if (name != null) it[UsersTable.name] = name
            if (email != null) it[UsersTable.email] = email
            if (hashedPassword != null) it[UsersTable.hashedPassword] = hashedPassword
        }

        UsersTable.selectAll()
            .where {UsersTable.id eq id}
            .map { toUserResponse(it) }
            .single()
    }

    private fun toUserResponse(row: ResultRow): UserResponse {
        return UserResponse(
            id = row[UsersTable.id],
            name = row[UsersTable.name],
            email = row[UsersTable.email]
        )
    }
}
