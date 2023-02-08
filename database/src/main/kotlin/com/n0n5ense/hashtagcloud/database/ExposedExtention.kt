package com.n0n5ense.hashtagcloud.database

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.DeleteStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager


internal fun <T: Table> T.deleteWhere(
    limit: Int? = null,
    offset: Long? = null,
    op: SqlExpressionBuilder.() -> Op<Boolean>
) = DeleteStatement.where(
    TransactionManager.current(),
    this@deleteWhere,
    op(SqlExpressionBuilder),
    false,
    limit,
    offset
)