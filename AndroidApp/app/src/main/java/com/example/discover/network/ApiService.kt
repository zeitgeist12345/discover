package com.example.discover.network

import com.example.discover.data.AddWebsiteRequest
import com.example.discover.data.Website
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ApiService {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api"
    private val jsonMediaType = "application/json".toMediaType()

    suspend fun getWebsites(): List<Website> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/getWebsites")
                .get()
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val json = response.body?.string() ?: "[]"
                gson.fromJson(json, Array<Website>::class.java).toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun incrementView(websiteId: String, websiteUrl: String, action: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/incrementView?id=$websiteId&url=${java.net.URLEncoder.encode(websiteUrl, "UTF-8")}&action=$action")
                .post("".toRequestBody())
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addWebsite(request: AddWebsiteRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(request)
            val requestBody = json.toRequestBody(jsonMediaType)
            
            val httpRequest = Request.Builder()
                .url("$baseUrl/addWebsite")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(httpRequest).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
} 