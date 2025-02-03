package domain.modules.expenses.models

import domain.validation.Validatable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isPositive
import org.valiktor.validate

data class UpdateExpenseRequest(
    val reason: String?,
    val amount: Double?,
    val categoryId: Int?,
): Validatable {
    override fun validate() {
        validate(this) {
            validate(UpdateExpenseRequest::reason).isNotBlank().hasSize(min = 3, max = 30)
            validate(UpdateExpenseRequest::amount).isPositive()
        }
    }
}
