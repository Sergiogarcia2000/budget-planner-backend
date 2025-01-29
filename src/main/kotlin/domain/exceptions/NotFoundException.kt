package domain.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class NotFoundException(override val message: String?) : RuntimeException("Entity not found")