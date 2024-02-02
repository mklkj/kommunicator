package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table

object UserPushTokensTable : Table("user_push_tokens") {

    val token = varchar("token", 200)
    val deviceIdHash = varchar("device_id_hash", 64)
    val deviceName = varchar("device_name", 32).nullable()
    val userId = kotlinxUUID("user_id")

    init {
        uniqueIndex("token_device_unique", token, deviceIdHash, userId)
    }
}
