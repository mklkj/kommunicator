package io.github.mklkj.kommunicator.data.columns

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.statements.jdbc.JdbcPreparedStatementImpl
import kotlin.Array
import java.sql.Array as SQLArray

/**
 * Implementation of [ColumnType] for the SQL `ARRAY` type.
 *
 * @see [https://gist.github.com/DRSchlaubi/cb146ee2b4d94d1c89b17b358187c612]
 *
 * @property underlyingType the type of the array
 * @property size an optional size of the array
 */
class ArrayColumnType(
    private val underlyingType: String,
    private val size: Int?
) : ColumnType() {

    override fun sqlType(): String = "$underlyingType ARRAY${size?.let { "[$it]" } ?: ""}"

    override fun notNullValueToDB(value: Any): Any = when (value) {
        is Array<*> -> value
        is Collection<*> -> value.toTypedArray()
        else -> error("Got unexpected array value of type: ${value::class.qualifiedName} ($value)")
    }

    override fun valueFromDB(value: Any): Any = when (value) {
        is SQLArray -> value.array as Array<*>
        is Array<*> -> value
        is Collection<*> -> value.toTypedArray()
        else -> error("Got unexpected array value of type: ${value::class.qualifiedName} ($value)")
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        if (value == null) {
            stmt.setNull(index, this)
        } else {
            val preparedStatement = stmt as? JdbcPreparedStatementImpl
                ?: error("Currently only JDBC is supported")
            val array = preparedStatement.statement.connection.createArrayOf(
                underlyingType,
                value as Array<*>
            )
            stmt[index] = array
        }
    }
}
