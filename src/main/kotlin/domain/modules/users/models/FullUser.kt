package domain.modules.users.models

data class FullUser(
    val id: Int,
    val username: String,
    val email: String,
    val hashedPassword: String
)
