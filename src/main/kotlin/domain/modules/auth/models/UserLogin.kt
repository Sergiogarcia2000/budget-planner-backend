package domain.modules.auth.models

import domain.validation.Validatable
import kotlinx.serialization.Serializable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class UserLogin(
    val email: String,
    val password: String
): Validatable {
    override fun validate() {
        validate(this) {
            validate(UserLogin::email).isNotBlank().isEmail()
            validate(UserLogin::password).isNotBlank().hasSize(min = 8, max = 16)
        }
    }
}
