
package com.example.ubuntumentor

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "UbuntuMentorSession",
        Context.MODE_PRIVATE
    )

    fun saveSession(
        userId: String,
        name: String,
        authType: String,
        token: String? = null
    ) {
        preferences.edit()
            .putBoolean("isLoggedIn", true)
            .putString("userId", userId)
            .putString("userName", name)
            .putString("authType", authType)
            .putString("authToken", token)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean("isLoggedIn", false)
    }

    fun getUserName(): String {
        return preferences.getString("userName", "") ?: ""
    }

    fun getUserId(): String {
        return preferences.getString("userId", "") ?: ""
    }

    fun getAuthType(): String {
        return preferences.getString("authType", "") ?: ""
    }

    fun getToken(): String {
        return preferences.getString("authToken", "") ?: ""
    }

    // Notification preference
    fun areNotificationsEnabled(): Boolean {
        return preferences.getBoolean("notificationsEnabled", true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean("notificationsEnabled", enabled)
            .apply()
    }

    fun logout() {
        preferences.edit().clear().apply()
    }
}
