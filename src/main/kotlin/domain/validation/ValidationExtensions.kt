package domain.validation

import domain.exceptions.ValidationException
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage
import java.util.*

/**
 * Extensión para validar cualquier objeto que implemente `Validatable`.
 */
fun <T> T.validateRequest(): Map<String, List<String>> where T : Validatable {
    return try {
        this.validate()
        emptyMap()
    } catch (e: ConstraintViolationException) {
        e.constraintViolations
            .mapToMessage(baseName = "messages", locale = Locale.ENGLISH)
            .groupBy({ it.property }, { it.message })
    }
}

/**
 * Función de extensión que valida un objeto Validatable y ejecuta la lógica de negocio si es válido.
 */
suspend fun <T : Validatable, R> T.validateAndProcess(block: suspend (T) -> Result<R>): Result<R> {
    val validationErrors = this.validateRequest()
    return if (validationErrors.isNotEmpty()) {
        Result.failure(ValidationException(validationErrors))
    } else {
        block(this)
    }
}