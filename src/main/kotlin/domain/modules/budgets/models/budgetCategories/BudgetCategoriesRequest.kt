package domain.modules.budgets.models.budgetCategories

import domain.validation.Validatable
import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.validateForEach
import org.valiktor.validate

@Serializable
data class BudgetCategoriesRequest (
    val categories: Set<Int>
): Validatable {
    override fun validate() {
        validate(this) {
            validate(BudgetCategoriesRequest::categories).isNotEmpty().validateForEach { value -> value > 0 }
        }
    }
}