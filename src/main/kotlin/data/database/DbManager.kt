package data.database

import EnvironmentHandler
import data.entities.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DbManager {
    private var initialized = false

    fun start(): Database {

        if (!initialized) {
            val database = Database.connect(
                url = EnvironmentHandler.DATABASE_URL,
                driver = EnvironmentHandler.DATABASE_DRIVER,
                user = EnvironmentHandler.DATABASE_USER,
                password = EnvironmentHandler.DATABASE_PASSWORD
            )

            transaction {
                SchemaUtils.create(UsersTable)
            }

            initialized = true
            return database
        } else {
            throw IllegalStateException("Database already initialized!")
        }
    }
    suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction { block() }
}