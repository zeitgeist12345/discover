package com.example.discover.network

import android.util.Log
import com.example.discover.data.Link
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// Define possible outcomes for the addWebsite operation
sealed class AddWebsiteResult {
    object Success : AddWebsiteResult()
    object Duplicate : AddWebsiteResult() // Specifically for 409
    data class Error(val message: String) : AddWebsiteResult()
    object NetworkError : AddWebsiteResult()
}

class ApiService {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api"
    private val jsonMediaType = "application/json".toMediaType()
    suspend fun getWebsites(): List<Link> = withContext(Dispatchers.IO) {
        try {
            Log.d("ApiService", "Fetching websites from: $baseUrl/getWebsites")

            val request = Request.Builder()
                .url("$baseUrl/getWebsites")
                .get()
                .build()

            client.newCall(request).execute()
                .use { response -> // This 'use' block needs to return List<Link>
                    Log.d("ApiService", "Response code: ${response.code}")

                    if (response.isSuccessful) {
                        // response.body() is guaranteed to be non-null if isSuccessful is true,
                        // but using ?.use is safer for the body itself.
                        val json = response.body.string()
                        Log.d("ApiService", "Response body: $json")
                        val downloadedWebsites = gson.fromJson(json, Array<Link>::class.java)
                        Log.d("ApiService", "Parsed ${downloadedWebsites.size} websites")
                        Log.d(
                            "ApiService",
                            "First website: ${downloadedWebsites.firstOrNull()?.name}"
                        )

                        // gson.fromJson returns Array<Link> with Array<Link>::class.java
                        val downloadedArray: Array<Link> =
                            gson.fromJson(json, Array<Link>::class.java)
                        Log.d("ApiService", "Parsed ${downloadedArray.size} websites")
                        Log.d("ApiService", "First website: ${downloadedArray.firstOrNull()?.name}")

                        // Convert Array<Link> to List<Link> before returning from the 'use' block
                        return@use downloadedArray.toList() // THIS IS THE KEY CHANGE
                    } else {
                        Log.e("ApiService", "HTTP error: ${response.code} - ${response.message}")
                        // Explicitly return List<Link>
                        return@use emptyList<Link>()
                    }
                } // The 'use' block now correctly returns List<Link>
        } catch (e: Exception) {
            Log.e("ApiService", "Exception fetching websites", e)
            // Explicitly return List<Link> for the catch block
            return@withContext emptyList<Link>()
        }
    }


    suspend fun incrementView(websiteId: String, websiteUrl: String?, action: String): Boolean =
        // websiteUrl is nullable
        withContext(Dispatchers.IO) {
            Log.d(
                "ApiService",
                "incrementView called with id: '$websiteId', url: '$websiteUrl', action: '$action'"
            ) // Logging input parameters
            if (websiteUrl == null) {
                Log.e("ApiService", "incrementView failed: websiteUrl is null for id $websiteId")
                Log.d("myapp", Log.getStackTraceString(java.lang.Exception()))
                return@withContext false
            }

            try {
                Log.d(
                    "ApiService",
                    "Encoding URL: '$websiteUrl' for incrementView."
                ) // Added logging

                val request = Request.Builder()
                    .url(
                        "$baseUrl/incrementView?id=$websiteId&url=${
                            java.net.URLEncoder.encode(
                                websiteUrl, // Now guaranteed to be non-null here
                                "UTF-8"
                            )
                        }&action=$action"
                    )
                    .post("".toRequestBody())
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(
                            "ApiService",
                            "incrementView failed: ${response.code} - ${response.message}"
                        )
                    }
                    response.isSuccessful
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Exception in incrementView", e)
                false
            }
        }

    suspend fun addWebsite(request: Link): AddWebsiteResult = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(request)
            Log.d("ApiService", "Adding website with JSON: $json")
            val requestBody = json.toRequestBody(jsonMediaType)

            val httpRequest = Request.Builder()
                .url("$baseUrl/addWebsite")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            Log.d("ApiService", "The httpRequest is $httpRequest")
            client.newCall(httpRequest).execute().use { response ->
                if (response.isSuccessful) {
                    AddWebsiteResult.Success
                } else {
                    val errorBody =
                        response.body.string() // Read error body for more details if needed
                    Log.w(
                        "ApiService",
                        "addWebsite failed: ${response.code} - ${response.message}. Body: $errorBody"
                    )
                    if (response.code == 409) { // HTTP 409 Conflict
                        AddWebsiteResult.Duplicate
                    } else {
                        AddWebsiteResult.Error(
                            "Failed to add website: ${response.message}"
                        )
                    }
                }
            }
        } catch (e: IOException) { // More specific catch for network issues
            Log.e("ApiService", "Network exception adding website", e)
            AddWebsiteResult.NetworkError
        } catch (e: Exception) { // Generic catch for other issues (e.g., JSON parsing)
            Log.e("ApiService", "Exception adding website", e)
            AddWebsiteResult.Error("An unexpected error occurred: ${e.message}")
        }
    }
} 