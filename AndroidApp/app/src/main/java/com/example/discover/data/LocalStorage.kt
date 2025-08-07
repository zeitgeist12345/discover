package com.example.discover.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class LocalStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("discover_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_WEBSITES = "cached_websites"
        private const val KEY_LAST_UPDATE = "last_update"
    }
    
    fun saveWebsites(websites: List<Website>) {
        val json = gson.toJson(websites)
        prefs.edit {
            putString(KEY_WEBSITES, json)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
        }
    }
    
    fun getCachedWebsites(): List<Website> {
        val json = prefs.getString(KEY_WEBSITES, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<Website>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}