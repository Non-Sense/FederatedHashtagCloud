package com.n0n5ense.hashtagcloud.database.datasource

import com.n0n5ense.hashtagcloud.common.TagData
import com.n0n5ense.hashtagcloud.database.ExcludeTagTable
import com.n0n5ense.hashtagcloud.database.HashTagDatabase
import com.n0n5ense.hashtagcloud.database.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExcludeTagDataSource(
    database: HashTagDatabase
) {
    private val db = database.database

    fun add(tag: String): Boolean {
        return transaction(db) {
            ExcludeTagTable.insertIgnore {
                it[tagName] = tag
            }.insertedCount != 0
        }
    }

    fun getAll(): List<TagData> {
        return transaction(db) {
            ExcludeTagTable.selectAll().map {
                TagData(it[ExcludeTagTable.tagName], it[ExcludeTagTable.createdAt])
            }
        }
    }

    fun remove(tag: String): Boolean {
        return transaction(db) {
            ExcludeTagTable.deleteWhere {
                ExcludeTagTable.tagName eq tag
            }
        } != 0
    }
}