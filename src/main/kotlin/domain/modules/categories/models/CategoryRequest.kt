package domain.modules.categories.models

import domain.validation.Validatable
import kotlinx.serialization.Serializable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class CategoryRequest(
    val name: String,
): Validatable {
    override fun validate() {
        validate(this) {
            validate(CategoryRequest::name).isNotBlank().hasSize(min = 3, max = 20)
        }
    }
}
