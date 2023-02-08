package com.n0n5ense.hashtagcloud.database.datasource

import com.n0n5ense.hashtagcloud.common.AggregatedTagData
import com.n0n5ense.hashtagcloud.common.TagData
import com.n0n5ense.hashtagcloud.database.ExcludeTagTable
import com.n0n5ense.hashtagcloud.database.HashTagDatabase
import com.n0n5ense.hashtagcloud.database.HashTagTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HashTagDataSource(
    database: HashTagDatabase
) {

    private val db = database.database

    fun add(data: TagData) {
        transaction(db) {
            HashTagTable.insert {
                it[tagName] = data.name
                it[createdAt] = data.createdAt
            }
        }
    }

    fun addAll(data: Iterable<TagData>) {
        transaction(db) {
            HashTagTable.batchInsert(data) {
                this[HashTagTable.tagName] = it.name
                this[HashTagTable.createdAt] = it.createdAt
            }
        }
    }

    fun aggregateWithExclude(from: Instant, limit: Int): List<AggregatedTagData> {
        return transaction(db) {
            HashTagTable
                .slice(HashTagTable.tagName, HashTagTable.tagName.count(), HashTagTable.createdAt.max())
                .select {
                    (HashTagTable.createdAt greaterEq from)
                }
                .groupBy(HashTagTable.tagName)
                .having { notExists(ExcludeTagTable.select { ExcludeTagTable.tagName eq HashTagTable.tagName }) }
                .orderBy((HashTagTable.tagName.count() to SortOrder.DESC), (HashTagTable.createdAt.max() to SortOrder.DESC))
                .limit(limit)
                .map { AggregatedTagData(it[HashTagTable.tagName], it[HashTagTable.tagName.count()],
                    (it[HashTagTable.createdAt.max()] as Instant).toUtcString()
                ) }
        }
    }

    fun aggregate(): List<AggregatedTagData> {
        return transaction(db) {
            HashTagTable
                .slice(HashTagTable.tagName, HashTagTable.tagName.count(), HashTagTable.createdAt.max())
                .selectAll()
                .groupBy(HashTagTable.tagName)
                .orderBy((HashTagTable.tagName.count() to SortOrder.DESC), (HashTagTable.createdAt.max() to SortOrder.DESC))
                .map { AggregatedTagData(it[HashTagTable.tagName], it[HashTagTable.tagName.count()],
                    (it[HashTagTable.createdAt.max()] as Instant).toUtcString()
                ) }
        }
    }

    fun getAll(): List<TagData> {
        return transaction(db) {
            HashTagTable.selectAll().map { TagData(it[HashTagTable.tagName], it[HashTagTable.createdAt]) }
        }
    }

    private fun Instant.toUtcString(): String {
        return this.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT)
    }
}

