package com.sahed.money_tracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sahed.money_tracker.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun getProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid).collection("profile").document("info")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
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
        val snapshot = firestore.collection("users").document(uid)
            .collection("profile").document("info")
            .get()
            .await()
        return if (snapshot.exists() && snapshot.data != null) {
            UserProfile.fromMap(snapshot.data!!)
        } else {
            null
        }
    }

    suspend fun updateProfile(uid: String, profile: UserProfile) {
        firestore.collection("users").document(uid)
            .collection("profile").document("info")
            .set(profile.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun syncGoogleProfile(uid: String, name: String, email: String, photoUrl: String) {
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl
        )
        firestore.collection("users").document(uid)
            .collection("profile").document("info")
            .set(updates, SetOptions.merge())
            .await()
    }
}
