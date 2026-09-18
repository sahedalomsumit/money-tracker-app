package com.sahed.money_tracker.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sahed.money_tracker.data.model.IncomeEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EntryRepository {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("EntryRepository", "Firestore not available: ${e.message}")
            null
        }

    fun getEntriesForYearFlow(uid: String, year: Int): Flow<List<IncomeEntry>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val colRef = db.collection("users").document(uid)
            .collection("entries")
            .whereEqualTo("year", year)

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("EntryRepository", "Error listening to year entries: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val entries = snapshot.documents.map { doc ->
                    IncomeEntry.fromMap(doc.id, doc.data ?: emptyMap())
                }.sortedWith(
                    compareByDescending<IncomeEntry> { it.month }
                        .thenByDescending { it.createdAt }
                )
                trySend(entries)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getAllEntriesFlow(uid: String): Flow<List<IncomeEntry>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val colRef = db.collection("users").document(uid).collection("entries")
            .orderBy("year", Query.Direction.DESCENDING)

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("EntryRepository", "Error listening to all entries: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val entries = snapshot.documents.map { doc ->
                    IncomeEntry.fromMap(doc.id, doc.data ?: emptyMap())
                }
                trySend(entries)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addEntry(uid: String, entry: IncomeEntry): String {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        val docRef = db.collection("users").document(uid)
            .collection("entries").document()
        val toSave = entry.copy(id = docRef.id)
        docRef.set(toSave.toMap()).await()
        return docRef.id
    }

    suspend fun updateEntry(uid: String, entry: IncomeEntry) {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        db.collection("users").document(uid)
            .collection("entries").document(entry.id)
            .set(entry.toMap(), SetOptions.merge())
            .await()
    }

    suspend fun deleteEntry(uid: String, entryId: String) {
        val db = firestore ?: throw IllegalStateException("Firestore not available")
        db.collection("users").document(uid)
            .collection("entries").document(entryId)
            .delete()
            .await()
    }
}
