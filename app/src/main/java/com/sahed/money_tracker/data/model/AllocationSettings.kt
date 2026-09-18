package com.sahed.money_tracker.data.model

data class AllocationSettings(
    val savingPercent: Double = 30.0,
    val savingLabel: String = "Saving",
    val investPercent: Double = 3.0,
    val investLabel: String = "Investing",
    val donatePercent: Double = 1.0,
    val donateLabel: String = "Donate",
    val restLabel: String = "Rest"
) {
    val restPercent: Double
        get() = (100.0 - (savingPercent + investPercent + donatePercent)).coerceAtLeast(0.0)

    val totalAllocatedPercent: Double
        get() = savingPercent + investPercent + donatePercent

    val isValid: Boolean
        get() = totalAllocatedPercent <= 100.0

    fun toMap(): Map<String, Any> {
        return mapOf(
            "savingPercent" to savingPercent,
            "savingLabel" to savingLabel,
            "investPercent" to investPercent,
            "investLabel" to investLabel,
            "donatePercent" to donatePercent,
            "donateLabel" to donateLabel,
            "restLabel" to restLabel
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): AllocationSettings {
            return AllocationSettings(
                savingPercent = (map["savingPercent"] as? Number)?.toDouble() ?: 30.0,
                savingLabel = map["savingLabel"] as? String ?: "Saving",
                investPercent = (map["investPercent"] as? Number)?.toDouble() ?: 3.0,
                investLabel = map["investLabel"] as? String ?: "Investing",
                donatePercent = (map["donatePercent"] as? Number)?.toDouble() ?: 1.0,
                donateLabel = map["donateLabel"] as? String ?: "Donate",
                restLabel = map["restLabel"] as? String ?: "Rest"
            )
        }
    }
}
