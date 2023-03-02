package com.n0n5ense.hashtagcloud.database.datasource

import com.n0n5ense.hashtagcloud.common.ExcludeUser
import com.n0n5ense.hashtagcloud.database.ExcludeUserTable
import com.n0n5ense.hashtagcloud.database.HashTagDatabase
import com.n0n5ense.hashtagcloud.database.deleteWhere
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExcludeUserDataSource(
    database: HashTagDatabase
) {
    private val db = database.database

    fun add(user: ExcludeUser): Boolean {
        return transaction(db) {
            ExcludeUserTable.insert {
                it[userName] = user.name
                it[domain] = user.domain
            }
        }.insertedCount != 0
    }

    fun getAll(): List<ExcludeUser> {
        return transaction(db) {
            ExcludeUserTable.selectAll().map {
                ExcludeUser(
                    it[ExcludeUserTable.userName],
                    it[ExcludeUserTable.domain]
                )
            }
        }
    }

    fun remove(user: ExcludeUser): Boolean {
        return transaction(db) {
            ExcludeUserTable.deleteWhere {
                (ExcludeUserTable.userName eq user.name) and
                        (ExcludeUserTable.domain eq user.domain)
            }
        } != 0
    }

}