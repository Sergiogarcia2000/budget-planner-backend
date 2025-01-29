package domain.modules.users.models

import domain.validation.Validatable
import kotlinx.serialization.Serializable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class UpdateUserRequest(
    val username: String?,
    val email: String?,
    val password: String?,
): Validatable {
    override fun validate() {
        validate(this) {
            validate(UpdateUserRequest::username).isNotBlank().hasSize(min = 3, max = 50)
            validate(UpdateUserRequest::email).isNotBlank().isEmail()
            validate(UpdateUserRequest::password).isNotBlank().hasSize(min = 8, max = 16)
        }
    }
}
