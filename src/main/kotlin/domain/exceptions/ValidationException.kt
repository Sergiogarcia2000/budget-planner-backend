package domain.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class ValidationException(val errors: Map<String, List<String>>) : RuntimeException("Validation failed")
