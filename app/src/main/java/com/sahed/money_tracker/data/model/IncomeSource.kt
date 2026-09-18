package com.sahed.money_tracker.data.model

data class MainSource(
    val id: String = "",
    val name: String = "",
    val order: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "order" to order
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): MainSource {
            return MainSource(
                id = id,
                name = map["name"] as? String ?: "",
                order = (map["order"] as? Number)?.toInt() ?: 0
            )
        }
    }
}

data class SubSource(
    val id: String = "",
    val name: String = "",
    val order: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "order" to order
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): SubSource {
            return SubSource(
                id = id,
                name = map["name"] as? String ?: "",
                order = (map["order"] as? Number)?.toInt() ?: 0
            )
        }
    }
}
