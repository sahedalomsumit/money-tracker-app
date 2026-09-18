package com.sahed.money_tracker.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sahed.money_tracker.data.model.AllocationSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AllocationRepository {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("AllocationRepository", "Firestore not available: ${e.message}")
            null
        }

    fun getAllocationFlow(uid: String): Flow<AllocationSettings> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(AllocationSettings())
            close()
            return@callbackFlow
        }

        val docRef = db.collection("users").document(uid)
            .collection("settings").document("allocation")

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("AllocationRepository", "Error listening to allocation: ${error.message}")
                trySend(AllocationSettings())
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                trySend(AllocationSettings.fromMap(snapshot.data!!))
            } else {
                trySend(AllocationSettings()) // Default values
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateAllocation(uid: String, settings: AllocationSettings) {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        db.collection("users").document(uid)
            .collection("settings").document("allocation")
            .set(settings.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun seedDefaultAllocationIfMissing(uid: String) {
        val db = firestore ?: return
        val docRef = db.collection("users").document(uid)
            .collection("settings").document("allocation")
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            docRef.set(AllocationSettings().toMap()).await()
        }
    }
}
