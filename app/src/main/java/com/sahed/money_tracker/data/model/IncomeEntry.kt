package com.sahed.money_tracker.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class IncomeEntry(
    val id: String = "",
    val month: Int = 1,
    val year: Int = 2026,
    val netSalary: Double = 0.0,
    val mainSourceId: String = "",
    val mainSourceName: String = "",
    val subSourceId: String? = null,
    val subSourceName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "month" to month,
            "year" to year,
            "netSalary" to netSalary,
            "mainSourceId" to mainSourceId,
            "mainSourceName" to mainSourceName,
            "subSourceId" to subSourceId,
            "subSourceName" to subSourceName,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): IncomeEntry {
            return IncomeEntry(
                id = id,
                month = (map["month"] as? Number)?.toInt() ?: 1,
                year = (map["year"] as? Number)?.toInt() ?: 2026,
                netSalary = (map["netSalary"] as? Number)?.toDouble() ?: 0.0,
                mainSourceId = map["mainSourceId"] as? String ?: "",
                mainSourceName = map["mainSourceName"] as? String ?: "",
                subSourceId = map["subSourceId"] as? String,
                subSourceName = map["subSourceName"] as? String,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
