package com.example.discover.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("discover_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_WEBSITES = "cached_websites"
        private const val KEY_LAST_UPDATE = "last_update"
        private const val CACHE_UPDATE_THRESHOLD_HOURS = 12
    }
    
    fun saveWebsites(websites: List<Website>) {
        val json = gson.toJson(websites)
        prefs.edit()
            .putString(KEY_WEBSITES, json)
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }
    
    fun getCachedWebsites(): List<Website> {
        val json = prefs.getString(KEY_WEBSITES, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<Website>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun hasCachedData(): Boolean {
        return prefs.contains(KEY_WEBSITES)
    }
    
    fun getLastUpdateTime(): Long {
        return prefs.getLong(KEY_LAST_UPDATE, 0)
    }
    
    fun isCacheValid(maxAgeHours: Int = 24): Boolean {
        val lastUpdate = getLastUpdateTime()
        val currentTime = System.currentTimeMillis()
        val maxAgeMs = maxAgeHours * 60 * 60 * 1000L
        return (currentTime - lastUpdate) < maxAgeMs
    }
    
    fun shouldUpdateCache(): Boolean {
//        val lastUpdate = getLastUpdateTime()
//        val currentTime = System.currentTimeMillis()
//        val thresholdMs = CACHE_UPDATE_THRESHOLD_HOURS * 60 * 60 * 1000L
//        return (currentTime - lastUpdate) >= thresholdMs
        return true
    }
    
    fun clearCache() {
        prefs.edit()
            .remove(KEY_WEBSITES)
            .remove(KEY_LAST_UPDATE)
            .apply()
    }
} 