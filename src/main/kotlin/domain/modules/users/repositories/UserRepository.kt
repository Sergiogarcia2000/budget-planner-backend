package domain.modules.users.repositories

import data.entities.UsersTable
import data.database.DbManager.dbQuery
import domain.modules.users.models.FullUser
import domain.modules.users.models.UserResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class UserRepository {

    suspend fun getAllUsers(): List<UserResponse> = dbQuery {
        UsersTable.selectAll().map { it.toUserResponse() }
    }

    suspend fun getUserById(userId: Int): UserResponse? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.id eq userId}
            .map { it.toUserResponse() }
            .singleOrNull()
    }

    suspend fun getUserByEmail(email: String): UserResponse? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.email eq email}
            .map { it.toUserResponse() }
            .singleOrNull()
    }

    suspend fun getFullUserByEmail(email: String): FullUser? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.email eq email}
            .map { it.toFullUser() }
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
            .map { it.toUserResponse() }
            .single()
    }

    suspend fun deleteUser(userId: Int): Boolean = dbQuery {
        UsersTable.deleteWhere { UsersTable.id eq userId } > 0
    }

    suspend fun updateUser(userId: Int, name: String?, email: String?, hashedPassword: String?): UserResponse = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            if (name != null) it[UsersTable.name] = name
            if (email != null) it[UsersTable.email] = email
            if (hashedPassword != null) it[UsersTable.hashedPassword] = hashedPassword
        }

        UsersTable.selectAll()
            .where {UsersTable.id eq userId}
            .map { it.toUserResponse() }
            .single()
    }

    private fun ResultRow.toUserResponse() = UserResponse(
        id = this[UsersTable.id],
        name = this[UsersTable.name],
        email = this[UsersTable.email]
    )

    private fun ResultRow.toFullUser() = FullUser(
        id = this[UsersTable.id],
        username = this[UsersTable.name],
        email = this[UsersTable.email],
        hashedPassword = this[UsersTable.hashedPassword]
    )
}
