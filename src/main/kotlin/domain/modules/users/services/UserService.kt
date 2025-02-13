package domain.modules.users.services

import domain.exceptions.NotFoundException
import domain.modules.users.models.CreateUserRequest
import domain.modules.users.models.UpdateUserRequest
import domain.modules.users.models.UserResponse
import domain.modules.users.repositories.UserRepository
import domain.validation.validateAndProcess
import utils.HashUtils

class UserService(private val userRepository: UserRepository) {

    suspend fun getAllUsers(): List<UserResponse> = userRepository.getAllUsers()

    suspend fun getUserById(id: Int): UserResponse? = userRepository.getUserById(id)

    suspend fun createUser(request: CreateUserRequest): Result<UserResponse> {
        return request.validateAndProcess { body ->
            if (userRepository.getUserByEmail(body.email) != null) {
                return@validateAndProcess  Result.failure(IllegalArgumentException("User with email ${body.email} already exists"))
            }

            val hashedPassword = HashUtils.hashPassword(body.password)
            val user = userRepository.createUser(body.name, body.email, hashedPassword)

            Result.success(user)
        }
    }

    suspend fun updateUser(userId: Int, request: UpdateUserRequest): Result<UserResponse> {
        return request.validateAndProcess { body ->
            if (userRepository.getUserById(userId) == null) {
                return@validateAndProcess Result.failure(NotFoundException("User with id $userId not found"))
            }

            val hashedPassword = if (body.password != null) HashUtils.hashPassword(body.password) else null

            Result.success(userRepository.updateUser(userId, request.username, request.email, hashedPassword))
        }
    }

    suspend fun deleteUser(userId: Int): Result<Boolean> {
        if (userRepository.getUserById(userId) == null) {
            return Result.failure(NotFoundException("User with id $userId not found"))
        }

        return Result.success(userRepository.deleteUser(userId))
    }
}