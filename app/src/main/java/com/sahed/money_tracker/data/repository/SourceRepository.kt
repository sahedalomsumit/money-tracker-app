package com.sahed.money_tracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sahed.money_tracker.data.model.MainSource
import com.sahed.money_tracker.data.model.SubSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SourceRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun getMainSourcesFlow(uid: String): Flow<List<MainSource>> = callbackFlow {
        val colRef = firestore.collection("users").document(uid).collection("mainSources")
            .orderBy("order", Query.Direction.ASCENDING)

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
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
        val colRef = firestore.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .collection("subSources")
            .orderBy("order", Query.Direction.ASCENDING)

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
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
        val docRef = firestore.collection("users").document(uid)
            .collection("mainSources").document()
        val source = MainSource(id = docRef.id, name = name, order = order)
        docRef.set(source.toMap()).await()
        return docRef.id
    }

    suspend fun updateMainSource(uid: String, mainSource: MainSource) {
        firestore.collection("users").document(uid)
            .collection("mainSources").document(mainSource.id)
            .set(mainSource.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun deleteMainSource(uid: String, mainSourceId: String) {
        firestore.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .delete()
            .await()
    }

    suspend fun addSubSource(uid: String, mainSourceId: String, name: String, order: Int): String {
        val docRef = firestore.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .collection("subSources").document()
        val subSource = SubSource(id = docRef.id, name = name, order = order)
        docRef.set(subSource.toMap()).await()
        return docRef.id
    }

    suspend fun updateSubSource(uid: String, mainSourceId: String, subSource: SubSource) {
        firestore.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .collection("subSources").document(subSource.id)
            .set(subSource.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun deleteSubSource(uid: String, mainSourceId: String, subSourceId: String) {
        firestore.collection("users").document(uid)
            .collection("mainSources").document(mainSourceId)
            .collection("subSources").document(subSourceId)
            .delete()
            .await()
    }

    suspend fun seedDefaultSourcesIfMissing(uid: String) {
        val colRef = firestore.collection("users").document(uid).collection("mainSources")
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
    }
}
