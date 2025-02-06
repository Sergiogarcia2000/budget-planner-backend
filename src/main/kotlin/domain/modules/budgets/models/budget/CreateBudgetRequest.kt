package domain.modules.budgets.models.budget

import domain.validation.Validatable
import org.valiktor.functions.*
import org.valiktor.validate
import java.util.Date

data class CreateBudgetRequest(
    val name: String,
    val limit: Int,
    val duration: Int,
    val startDate: Date,
    val recurrent: Boolean
): Validatable {
    override fun validate() {
        validate(this) {
            validate(CreateBudgetRequest::name).isNotBlank().hasSize(min = 3, max = 30)
            validate(CreateBudgetRequest::limit).isPositive()
            validate(CreateBudgetRequest::duration).isPositive()
            validate(CreateBudgetRequest::startDate).isNotNull()
            validate(CreateBudgetRequest::recurrent).isNotNull()
        }
    }
}
