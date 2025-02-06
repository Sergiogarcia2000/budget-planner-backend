package domain.modules.budgets.models.budget

import domain.validation.Validatable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isPositive
import java.util.*

data class UpdateBudgetRequest(
    val name: String?,
    val limit: Int?,
    val duration: Int?,
    val startDate: Date?,
    val recurrent: Boolean?,
    val finished: Boolean?,
): Validatable {
    override fun validate() {
        org.valiktor.validate(this) {
            validate(UpdateBudgetRequest::name).isNotBlank().hasSize(min = 3, max = 30)
            validate(UpdateBudgetRequest::limit).isPositive()
            validate(UpdateBudgetRequest::duration).isPositive()
        }
    }
}
