package com.example.stressease.Settings

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import kotlinx.coroutines.flow.first

class SettingsRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dataStore = SettingsDataStore(context)

    private fun userDocPath(): Pair<String, String>? {
        val uid = auth.currentUser?.uid ?: return null
        return Pair("users", uid)
    }

    // Load from Firestore once (primary)
    suspend fun loadFromFirestoreOnce(): SettingsModel? {
        val pair = userDocPath() ?: return null
        return try {
            val doc = db.collection(pair.first)
                .document(pair.second)
                .collection("settings")
                .document("preferences")
                .get()
                .await()

            if (!doc.exists()) return null

            // read fields defensively
            val name = doc.getString("name") ?: ""
            val email = doc.getString("email") ?: ""
            val daily = (doc.getLong("dailyGoal") ?: 3L).toInt()
            val weekly = (doc.getLong("weeklyGoal") ?: 15L).toInt()
            val push = doc.getBoolean("pushEnabled") ?: true
            val emailNot = doc.getBoolean("emailEnabled") ?: false
            val reminders = doc.getBoolean("remindersEnabled") ?: true
            val biometric = doc.getBoolean("biometricLock") ?: false
            val dataSharing = doc.getBoolean("dataSharing") ?: true
            val last = doc.getLong("lastSyncedMillis") ?: System.currentTimeMillis()
            SettingsModel(
                name = name,
                email = email,
                dailyGoal = daily,
                weeklyGoal = weekly,
                pushEnabled = push,
                emailEnabled = emailNot,
                remindersEnabled = reminders,
                biometricLock = biometric,
                dataSharing = dataSharing,
                lastSyncedMillis = last
            )
        } catch (e: Exception) {
            Log.e("SettingsRepo", "loadFromFirestoreOnce failed", e)
            null
        }
    }

    // Save to Firestore (primary). Returns true on success.
    suspend fun saveToFirestore(model: SettingsModel): Boolean {
        val pair = userDocPath() ?: return false
        return try {
            val docRef = db.collection(pair.first)
                .document(pair.second)
                .collection("settings")
                .document("preferences")

            val payload = hashMapOf<String, Any>(
                "name" to model.name,
                "email" to model.email,
                "dailyGoal" to model.dailyGoal,
                "weeklyGoal" to model.weeklyGoal,
                "pushEnabled" to model.pushEnabled,
                "emailEnabled" to model.emailEnabled,
                "remindersEnabled" to model.remindersEnabled,
                "biometricLock" to model.biometricLock,
                "dataSharing" to model.dataSharing,
                "lastSyncedMillis" to System.currentTimeMillis()
            )

            docRef.set(payload).await()
            true
        } catch (e: Exception) {
            Log.e("SettingsRepo", "saveToFirestore failed", e)
            false
        }
    }

    // Save to local DataStore cache
    suspend fun saveToLocal(model: SettingsModel) {
        dataStore.saveSettings(model.copy(lastSyncedMillis = System.currentTimeMillis()))
    }

    // Load from local DataStore
    suspend fun loadFromLocal(): SettingsModel {
        return dataStore.settingsFlow.first()
    }

    // Sync: read Firestore and update DataStore if remote exists
    suspend fun syncCloudToLocalIfExists() {
        val remote = loadFromFirestoreOnce() ?: return
        saveToLocal(remote)
    }

    // Export as a JSON string (straightforward)
    suspend fun exportToJsonString(): String {
        val s = dataStore.settingsFlow.first()
        val j = JSONObject()
        j.put("name", s.name)
        j.put("email", s.email)
        j.put("dailyGoal", s.dailyGoal)
        j.put("weeklyGoal", s.weeklyGoal)
        j.put("pushEnabled", s.pushEnabled)
        j.put("emailEnabled", s.emailEnabled)
        j.put("remindersEnabled", s.remindersEnabled)
        j.put("biometricLock", s.biometricLock)
        j.put("dataSharing", s.dataSharing)
        j.put("lastSyncedMillis", s.lastSyncedMillis)
        return j.toString()
    }

    // Import JSON string (update Firestore first then DataStore)
    // returns Pair(success:Boolean, message:String)
    suspend fun importFromJsonString(json: String): Pair<Boolean, String> {
        return try {
            val j = JSONObject(json)
            val model = SettingsModel(
                name = j.optString("name", ""),
                email = j.optString("email", ""),
                dailyGoal = j.optInt("dailyGoal", 3),
                weeklyGoal = j.optInt("weeklyGoal", 15),
                pushEnabled = j.optBoolean("pushEnabled", true),
                emailEnabled = j.optBoolean("emailEnabled", false),
                remindersEnabled = j.optBoolean("remindersEnabled", true),
                biometricLock = j.optBoolean("biometricLock", false),
                dataSharing = j.optBoolean("dataSharing", true),
                lastSyncedMillis = j.optLong("lastSyncedMillis", System.currentTimeMillis())
            )

            // Save to Firestore primary
            val ok = saveToFirestore(model)
            if (!ok) return Pair(false, "Failed to write to Firestore")
            // then update local cache
            saveToLocal(model)
            Pair(true, "Imported successfully")
        } catch (e: Exception) {
            Pair(false, "Import parse error: ${e.message}")
        }
    }
}
