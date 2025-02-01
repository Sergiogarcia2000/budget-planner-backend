package domain.modules.auth.services

import application.auth.SecurityConfig
import domain.modules.auth.models.UserLogin
import domain.modules.users.models.CreateUserRequest
import domain.modules.users.repositories.UserRepository
import domain.validation.validateAndProcess
import utils.HashUtils

class AuthService(private val userRepository: UserRepository) {

    suspend fun register(request: CreateUserRequest): Result<String> {
        return request.validateAndProcess { body ->
            if (userRepository.getFullUserByEmail(body.email) != null) {
                return@validateAndProcess Result.failure(IllegalArgumentException("User with email already exists"))
            }

            val hashedPassword = HashUtils.hashPassword(body.password)
            val user = userRepository.createUser(body.name, body.email, hashedPassword)
            println(user)
            println("asd")
            try {
                println(SecurityConfig.generateToken(user.id, user.email))
            }catch (e: Exception) {
                e.printStackTrace()
            }
            println("dsa")
            Result.success(SecurityConfig.generateToken(user.id, user.email))
        }
    }

    suspend fun login(request: UserLogin): Result<String> {
        return request.validateAndProcess { body ->
            val user = userRepository.getFullUserByEmail(body.email)
                ?: return@validateAndProcess Result.failure(IllegalArgumentException("Invalid credentials"))

            if (!HashUtils.verifyPassword(body.password, user.hashedPassword)) {
                return@validateAndProcess Result.failure(IllegalArgumentException("Invalid credentials"))
            }

            Result.success(SecurityConfig.generateToken(user.id, user.email))
        }
    }
}