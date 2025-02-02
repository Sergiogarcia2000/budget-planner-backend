package data.extensions

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query

fun <T> Query.queryEqOptional(column: Column<T>, value: T?): Query {
    return let { if (value != null) it.where { column eq value } else it }
}

fun <T> Query.queryNeqOptional(column: Column<T>, value: T?): Query {
    return let { if (value != null) it.where { column neq value } else it }
}