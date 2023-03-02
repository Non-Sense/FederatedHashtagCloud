package com.n0n5ense.hashtagcloud.database.datasource

import com.n0n5ense.hashtagcloud.common.ExcludeDomain
import com.n0n5ense.hashtagcloud.database.ExcludeDomainTable
import com.n0n5ense.hashtagcloud.database.HashTagDatabase
import com.n0n5ense.hashtagcloud.database.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExcludeDomainDataSource(
    database: HashTagDatabase
) {
    private val db = database.database

    fun add(domain: ExcludeDomain): Boolean {
        return transaction(db) {
            ExcludeDomainTable.insert {
                it[this.domain] = domain.domain
            }
        }.insertedCount != 0
    }

    fun getAll(): List<ExcludeDomain> {
        return transaction(db) {
            ExcludeDomainTable.selectAll().map {
                ExcludeDomain(
                    it[ExcludeDomainTable.domain]
                )
            }
        }
    }

    fun remove(domain: ExcludeDomain): Boolean {
        return transaction(db) {
            ExcludeDomainTable.deleteWhere {
                (ExcludeDomainTable.domain eq domain.domain)
            }
        } != 0
    }

}