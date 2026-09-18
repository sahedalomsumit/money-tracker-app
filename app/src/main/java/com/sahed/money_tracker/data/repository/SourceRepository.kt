package com.sahed.money_tracker.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sahed.money_tracker.data.model.MainSource
import com.sahed.money_tracker.data.model.SubSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SourceRepository {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("SourceRepository", "Firestore not available: ${e.message}")
            null
        }

    fun getMainSourcesFlow(uid: String): Flow<List<MainSource>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val colRef = db.collection("users").document(uid).collection("mainSources")
            .orderBy("order", Query.Direction.ASCENDING)

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("SourceRepository", "Error listening to main sources: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val sources = snapshot.documents.map { doc ->
                    MainSource.fromMap(doc.id, doc.data ?: emptyMap())
                }
                trySend(sources)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getSubSourcesFlow(uid: String, mainSourceId: String): Flow<List<SubSource>> = callbackFlow {
        if (mainSourceId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val colRef = db.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .collection("subSources")
            .orderBy("order", Query.Direction.ASCENDING)

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("SourceRepository", "Error listening to sub sources: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val subSources = snapshot.documents.map { doc ->
                    SubSource.fromMap(doc.id, doc.data ?: emptyMap())
                }
                trySend(subSources)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addMainSource(uid: String, name: String, order: Int): String {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        val docRef = db.collection("users").document(uid)
            .collection("mainSources").document()
        val source = MainSource(id = docRef.id, name = name, order = order)
        docRef.set(source.toMap()).await()
        return docRef.id
    }

    suspend fun updateMainSource(uid: String, mainSource: MainSource) {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        db.collection("users").document(uid)
            .collection("mainSources").document(mainSource.id)
            .set(mainSource.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun deleteMainSource(uid: String, mainSourceId: String) {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        db.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .delete()
            .await()
    }

    suspend fun addSubSource(uid: String, mainSourceId: String, name: String, order: Int): String {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        val docRef = db.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .collection("subSources").document()
        val subSource = SubSource(id = docRef.id, name = name, order = order)
        docRef.set(subSource.toMap()).await()
        return docRef.id
    }

    suspend fun updateSubSource(uid: String, mainSourceId: String, subSource: SubSource) {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        db.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .collection("subSources").document(subSource.id)
            .set(subSource.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun deleteSubSource(uid: String, mainSourceId: String, subSourceId: String) {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        db.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .collection("subSources").document(subSourceId)
            .delete()
            .await()
    }

    suspend fun seedDefaultSourcesIfMissing(uid: String) {
        val db = firestore ?: return
        val colRef = db.collection("users").document(uid).collection("mainSources")
        try {
            val snapshot = colRef.limit(1).get().await()
            if (snapshot.isEmpty) {
                val defaultMainSources = listOf(
                    "Job",
                    "Business",
                    "Freelancing",
                    "Tax Return",
                    "Gift"
                )
                for ((index, name) in defaultMainSources.withIndex()) {
                    val mainDoc = colRef.document()
                    mainDoc.set(mapOf("name" to name, "order" to index)).await()

                    if (name == "Freelancing") {
                        val subSources = listOf("Fiverr", "Upwork", "Stripe", "Direct", "PayPal")
                        for ((subIndex, subName) in subSources.withIndex()) {
                            mainDoc.collection("subSources").document()
                                .set(mapOf("name" to subName, "order" to subIndex)).await()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SourceRepository", "Error seeding default sources: ${e.message}")
        }
    }
}
