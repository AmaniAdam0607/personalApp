package org.dromio.repository

import org.dromio.database.Debts
import org.dromio.models.Debt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DebtRepository {
    fun getAllDebts(): List<Debt> = transaction {
        Debts.selectAll()
            .orderBy(Debts.createdAt to SortOrder.DESC)
            .map { row ->
                Debt(
                    id = row[Debts.id].value,
                    customerName = row[Debts.customerName],
                    transactionId = row[Debts.transactionId].toString(),
                    amount = row[Debts.amount],
                    isPaid = row[Debts.isPaid],
                    createdAt = LocalDateTime.ofEpochSecond(row[Debts.createdAt], 0, ZoneOffset.UTC),
                    paidAt = row[Debts.paidAt]?.let {
                        LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
                    }
                )
            }
    }

    fun createDebt(customerName: String, transactionId: Int, amount: Double) = transaction {
        Debts.insert {
            it[Debts.customerName] = customerName
            it[Debts.transactionId] = transactionId
            it[Debts.amount] = amount
            it[createdAt] = System.currentTimeMillis() / 1000
        }
    }

    fun markAsPaid(debtId: Int) = transaction {
        Debts.update({ Debts.id eq debtId }) {
            it[isPaid] = true
            it[paidAt] = System.currentTimeMillis() / 1000
        }
    }
}
