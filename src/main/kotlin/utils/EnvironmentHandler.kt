import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig

object EnvironmentHandler {
    private lateinit var environmentConfig: ApplicationConfig

    fun init(application: Application) {
        environmentConfig = application.environment.config
    }

    operator fun get(key: String): String? {
        return environmentConfig.propertyOrNull(key)?.getString()
    }

    val DATABASE_URL: String by lazy { this["ktor.database.jdbcUrl"] ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" }
    val DATABASE_DRIVER: String by lazy { this["ktor.database.driverClassName"] ?: "org.mariadb.jdbc.Driver" }
    val DATABASE_USER: String by lazy { this["ktor.database.username"] ?: "defaultUser" }
    val DATABASE_PASSWORD: String by lazy { this["ktor.database.password"] ?: "defaultPassword" }
}