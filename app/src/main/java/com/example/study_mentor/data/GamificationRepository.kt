package com.example.study_mentor.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing user gamification and RPG experience progression in Cloud Firestore.
 * Document structure: users/{userId}/gamification/stats
 */
class GamificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val TAG = "GamificationRepo"
        private const val USERS_COLLECTION = "users"
        private const val GAMIFICATION_COLLECTION = "gamification"
        private const val STATS_DOCUMENT = "stats"
        private const val FIELD_TOTAL_XP = "totalXp"
        private const val FIELD_LAST_UPDATED = "lastUpdated"
    }

    /**
     * Atomically adds XP to the user's gamification stats using Firestore FieldValue.increment.
     * If the document does not exist yet, SetOptions.merge() creates it automatically.
     *
     * @param userId The Firebase UID of the target user.
     * @param earnedXp The positive experience points to award.
     */
    suspend fun addXp(userId: String, earnedXp: Int) {
        if (userId.isBlank() || earnedXp <= 0) return

        try {
            val statsRef = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(GAMIFICATION_COLLECTION)
                .document(STATS_DOCUMENT)

            val payload = mapOf(
                FIELD_TOTAL_XP to FieldValue.increment(earnedXp.toLong()),
                FIELD_LAST_UPDATED to System.currentTimeMillis()
            )

            statsRef.set(payload, SetOptions.merge()).await()
            Log.d(TAG, "Successfully awarded $earnedXp XP to user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding XP for user $userId", e)
            throw e
        }
    }

    /**
     * Observes real-time changes to the user's gamification stats via Firestore snapshot listener.
     * Emits a fresh [UserStats] whenever totalXp changes in the database.
     *
     * @param userId The Firebase UID of the target user.
     */
    fun getUserStats(userId: String): Flow<UserStats> = callbackFlow {
        if (userId.isBlank()) {
            trySend(UserStats(totalXp = 0))
            awaitClose { }
            return@callbackFlow
        }

        val statsRef = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(GAMIFICATION_COLLECTION)
            .document(STATS_DOCUMENT)

        val registration = statsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Firestore listener error for stats of user $userId", error)
                trySend(UserStats(totalXp = 0))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val totalXp = snapshot.getLong(FIELD_TOTAL_XP)?.toInt() ?: 0
                trySend(UserStats(totalXp = maxOf(0, totalXp)))
            } else {
                // Document not created yet -> Default to Level 1 Novice with 0 XP
                trySend(UserStats(totalXp = 0))
            }
        }

        awaitClose { registration.remove() }
    }

    /**
     * One-shot fetch of the user's current [UserStats].
     */
    suspend fun getStats(userId: String): UserStats {
        if (userId.isBlank()) return UserStats(totalXp = 0)

        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(GAMIFICATION_COLLECTION)
                .document(STATS_DOCUMENT)
                .get()
                .await()

            val xp = snapshot.getLong(FIELD_TOTAL_XP)?.toInt() ?: 0
            UserStats(totalXp = maxOf(0, xp))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user stats once for $userId", e)
            UserStats(totalXp = 0)
        }
    }
}
