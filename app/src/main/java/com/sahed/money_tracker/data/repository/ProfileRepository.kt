package com.sahed.money_tracker.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sahed.money_tracker.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Firestore not available: ${e.message}")
            null
        }

    fun getProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = db.collection("users").document(uid).collection("profile").document("info")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ProfileRepository", "Error listening to profile: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                trySend(UserProfile.fromMap(snapshot.data!!))
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getProfileOnce(uid: String): UserProfile? {
        val db = firestore ?: return null
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("profile").document("info")
                .get()
                .await()
            if (snapshot.exists() && snapshot.data != null) {
                UserProfile.fromMap(snapshot.data!!)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting profile once: ${e.message}")
            null
        }
    }

    suspend fun updateProfile(uid: String, profile: UserProfile) {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        db.collection("users").document(uid)
            .collection("profile").document("info")
            .set(profile.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun syncGoogleProfile(uid: String, name: String, email: String, photoUrl: String) {
        val db = firestore ?: return
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl
        )
        try {
            db.collection("users").document(uid)
                .collection("profile").document("info")
                .set(updates, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error syncing Google profile: ${e.message}")
        }
    }
}
