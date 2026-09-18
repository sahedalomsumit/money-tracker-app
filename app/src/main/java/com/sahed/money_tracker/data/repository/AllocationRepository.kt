package com.sahed.money_tracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sahed.money_tracker.data.model.AllocationSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AllocationRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun getAllocationFlow(uid: String): Flow<AllocationSettings> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
            .collection("settings").document("allocation")

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
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
        firestore.collection("users").document(uid)
            .collection("settings").document("allocation")
            .set(settings.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun seedDefaultAllocationIfMissing(uid: String) {
        val docRef = firestore.collection("users").document(uid)
            .collection("settings").document("allocation")
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            docRef.set(AllocationSettings().toMap()).await()
        }
    }
}
