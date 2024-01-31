package io.github.mklkj.kommunicator.data.db.adapters

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

object InstantStringAdapter : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = Instant.parse(databaseValue)
    override fun encode(value: Instant): String = value.toString()
}
