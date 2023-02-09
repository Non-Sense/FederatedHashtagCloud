package com.n0n5ense.hashtagcloud.database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.ds.PGSimpleDataSource
import java.time.Instant

class HashTagDatabase private constructor(internal val database: Database){

    companion object {
        fun connect(url: String, userName: String, password: String): HashTagDatabase {
            val db = Database.connect(PGSimpleDataSource().apply {
                setURL("jdbc:$url")
                user = userName
                this.password = password
                reWriteBatchedInserts = true
            })
            return HashTagDatabase(db)
        }
    }

    fun init() {
        transaction(database) {
            SchemaUtils.create(
                HashTagTable,
                ExcludeTagTable
            )
        }
    }
}

internal object HashTagTable : LongIdTable() {
    val tagName = text("tag_name")
    val userId = long("user_id")
    val createdAt = timestamp("created_at")
}

internal object ExcludeTagTable : LongIdTable() {
    val tagName = text("tag_name").uniqueIndex()
    val createdAt = timestamp("created_at").default(Instant.now())
}