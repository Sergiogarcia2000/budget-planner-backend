package data.extensions

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and

fun <T> Query.queryEqOptional(column: Column<T>, value: T?): Query {
    return let { if (value != null) it.where { column eq value } else it }
}

fun <T> Query.queryNeqOptional(column: Column<T>, value: T?): Query {
    return let { if (value != null) it.where { column neq value } else it }
}

/**
 * Si [value] no es nulo, combina la condición actual con la proporcionada en [op] usando AND.
 * Si es nulo, devuelve la condición actual sin cambios.
 */
fun <T> Op<Boolean>.andIfNotNull(value: T?, op: (T) -> Op<Boolean>): Op<Boolean> =
    if (value != null) this and op(value) else this