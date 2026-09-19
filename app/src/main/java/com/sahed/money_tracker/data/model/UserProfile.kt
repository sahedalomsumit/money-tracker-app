package com.sahed.money_tracker.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val sex: String = "",
    val country: String = "",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val onboarded: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl,
            "sex" to sex,
            "country" to country,
            "currencyCode" to currencyCode,
            "currencySymbol" to currencySymbol,
            "onboarded" to onboarded
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                name = map["name"] as? String ?: "",
                email = map["email"] as? String ?: "",
                photoUrl = map["photoUrl"] as? String ?: "",
                sex = map["sex"] as? String ?: "",
                country = map["country"] as? String ?: "",
                currencyCode = map["currencyCode"] as? String ?: "USD",
                currencySymbol = map["currencySymbol"] as? String ?: "$",
                onboarded = map["onboarded"] as? Boolean ?: false
            )
        }
    }
}
