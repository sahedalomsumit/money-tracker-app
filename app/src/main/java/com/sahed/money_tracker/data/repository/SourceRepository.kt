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

    suspend fun reorderMainSources(uid: String, sources: List<MainSource>) {
        val db = firestore ?: return
        val batch = db.batch()
        sources.forEachIndexed { index, source ->
            val docRef = db.collection("users").document(uid)
                .collection("mainSources").document(source.id)
            batch.update(docRef, "order", index)
        }
        batch.commit().await()
    }

    suspend fun reorderSubSources(uid: String, mainSourceId: String, subSources: List<SubSource>) {
        val db = firestore ?: return
        val batch = db.batch()
        subSources.forEachIndexed { index, subSource ->
            val docRef = db.collection("users").document(uid)
                .collection("mainSources").document(mainSourceId)
                .collection("subSources").document(subSource.id)
            batch.update(docRef, "order", index)
        }
        batch.commit().await()
    }

    suspend fun seedDefaultSourcesIfMissing(uid: String) {
        val db = firestore ?: return
        val colRef = db.collection("users").document(uid).collection("mainSources")
        try {
            val snapshot = colRef.get().await()
            if (snapshot.isEmpty) {
                val defaultMainSources = listOf(
                    "Job",
                    "Business",
                    "Freelancing",
                    "Govt. Benefits",
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
                    } else if (name == "Govt. Benefits") {
                        val subSources = listOf("Unemployment", "Housing Allowance", "Language")
                        for ((subIndex, subName) in subSources.withIndex()) {
                            mainDoc.collection("subSources").document()
                                .set(mapOf("name" to subName, "order" to subIndex)).await()
                        }
                    }
                }
            } else {
                // Ensure Govt. Benefits exists for existing users
                val govtDoc = snapshot.documents.find {
                    val n = it.getString("name")
                    n?.equals("Govt. Benefits", ignoreCase = true) == true ||
                    n?.equals("Govt Benefits", ignoreCase = true) == true
                }
                val govtDocRef = if (govtDoc == null) {
                    val newDoc = colRef.document()
                    newDoc.set(mapOf("name" to "Govt. Benefits", "order" to snapshot.size())).await()
                    newDoc
                } else {
                    govtDoc.reference
                }

                // Check sub-sources for Govt. Benefits
                val subCol = govtDocRef.collection("subSources")
                val subSnapshot = subCol.get().await()
                val existingSubNames = subSnapshot.documents.mapNotNull { it.getString("name")?.lowercase() }.toSet()
                val requiredSubs = listOf("Unemployment", "Housing Allowance", "Language")
                var nextOrder = subSnapshot.size()
                for (subName in requiredSubs) {
                    if (subName.lowercase() !in existingSubNames) {
                        subCol.document().set(mapOf("name" to subName, "order" to nextOrder++)).await()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SourceRepository", "Error seeding default sources: ${e.message}")
        }
    }
}
