package com.n0n5ense.hashtagcloud.database.datasource

import com.n0n5ense.hashtagcloud.common.AggregatedTagData
import com.n0n5ense.hashtagcloud.common.TagData
import com.n0n5ense.hashtagcloud.database.*
import com.n0n5ense.hashtagcloud.database.ExcludeTagTable
import com.n0n5ense.hashtagcloud.database.ExcludeUserTable
import com.n0n5ense.hashtagcloud.database.HashTagTable
import com.n0n5ense.hashtagcloud.database.deleteWhere
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HashTagDataSource(
    database: HashTagDatabase
) {

    companion object {
        private const val aggregateQuery = """SELECT tag_name, count(*) as count, max(tmax) as latest FROM (
SELECT d2.tag_name, d2.user_id, max(d2.created_at) as tmax FROM (
SELECT tag_name, user_id, user_name, t1.domain, created_at FROM (
SELECT tag_name, user_id, user_name, domain, created_at
FROM hashtag WHERE created_at > cast(? as timestamp)) AS t1
LEFT JOIN (SELECT domain from excludedomain) AS d
ON d.domain = t1.domain WHERE d.domain is null) as d2
LEFT JOIN (SELECT user_name, domain FROM excludeuser) AS u
ON u.domain = d2.domain AND u.user_name = d2.user_name WHERE u.user_name is null
GROUP BY tag_name, user_id) AS t2
GROUP BY tag_name ORDER BY count DESC, latest DESC LIMIT ?;"""

        private val sqlDatetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    }

    private val db = database.database

    fun add(data: TagData) {
        transaction(db) {
            HashTagTable.insert {
                it[tagName] = data.name
                it[userId] = data.userId
                it[createdAt] = data.createdAt
            }
        }
    }

    fun addAll(data: Iterable<TagData>) {
        transaction(db) {
            HashTagTable.batchInsert(data) {
                this[HashTagTable.tagName] = it.name
                this[HashTagTable.userId] = it.userId
                this[HashTagTable.createdAt] = it.createdAt
                this[HashTagTable.userName] = it.userName
                this[HashTagTable.domain] = it.domain
            }
        }
    }

    fun aggregateWithExclude(from: Instant, limit: Int): List<AggregatedTagData> {
        return transaction(db) {
            val statement = this.connection.prepareStatement(aggregateQuery, true).apply {
                set(1, from.toSqlString())
                set(2, limit)
            }
            val rs = statement.executeQuery()

            val res = mutableListOf<AggregatedTagData>()
            while(rs.next()) {
                res += AggregatedTagData(rs.getString(1), rs.getLong(2), rs.getTimestamp(3).toInstant().toUtcString())
            }
            res
        }
    }

    fun aggregate(): List<AggregatedTagData> {
        return transaction(db) {
            HashTagTable
                .slice(HashTagTable.tagName, HashTagTable.tagName.count(), HashTagTable.createdAt.max())
                .selectAll()
                .groupBy(HashTagTable.tagName)
                .orderBy(
                    (HashTagTable.tagName.count() to SortOrder.DESC),
                    (HashTagTable.createdAt.max() to SortOrder.DESC)
                )
                .map {
                    AggregatedTagData(
                        it[HashTagTable.tagName], it[HashTagTable.tagName.count()],
                        (it[HashTagTable.createdAt.max()] as Instant).toUtcString()
                    )
                }
        }
    }

    fun deleteWhere(until: Instant): Int {
        return transaction(db) {
            HashTagTable.deleteWhere {
                HashTagTable.createdAt lessEq until
            }
        }
    }

    fun getAll(): List<TagData> {
        return transaction(db) {
            HashTagTable.selectAll()
                .map {
                    TagData(
                        it[HashTagTable.tagName],
                        it[HashTagTable.userId],
                        it[HashTagTable.createdAt],
                        it[HashTagTable.userName],
                        it[HashTagTable.domain]
                    )
                }
        }
    }

    private fun Instant.toUtcString(): String {
        return this.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT)
    }

    private fun Instant.toSqlString(): String {
        return this.atZone(ZoneId.systemDefault()).format(sqlDatetimeFormatter)
    }
}

