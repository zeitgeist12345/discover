package com.example.discover.network

import android.util.Log
import com.example.discover.data.AddWebsiteRequest
import com.example.discover.data.Website
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
    suspend fun getWebsites(): List<Website> = withContext(Dispatchers.IO) {
        try {
            Log.d("ApiService", "Fetching websites from: $baseUrl/getWebsites")

            val request = Request.Builder()
                .url("$baseUrl/getWebsites")
                .get()
                .build()

            // Use client.newCall(request).execute().use { response -> ... }
            client.newCall(request).execute().use { response ->
                Log.d("ApiService", "Response code: ${response.code}")

                if (response.isSuccessful) {
                    // response.body() is guaranteed to be non-null if isSuccessful is true,
                    // but using ?.use is safer for the body itself.
                    val json = response.body.string()
                    Log.d("ApiService", "Response body: $json")

                    val websites = gson.fromJson(json, Array<Website>::class.java).toList()
                    Log.d("ApiService", "Parsed ${websites.size} websites")
                    websites // This will be the return value of the 'use' block
                } else {
                    Log.e("ApiService", "HTTP error: ${response.code} - ${response.message}")
                    // It's good practice to log the error message from the response as well
                    // The body of an error response might also contain useful info,
                    // but for now, just ensure it's closed by the 'use' block.
                    emptyList()
                }
            } // The response and its body are automatically closed here
        } catch (e: Exception) {
            Log.e("ApiService", "Exception fetching websites", e)
            emptyList()
        }
    }

    suspend fun incrementView(websiteId: String, websiteUrl: String, action: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(
                        "$baseUrl/incrementView?id=$websiteId&url=${
                            java.net.URLEncoder.encode(
                                websiteUrl,
                                "UTF-8"
                            )
                        }&action=$action"
                    )
                    .post("".toRequestBody()) // Empty body for POST
                    .build()

                client.newCall(request).execute().use { response ->
                    // Even if you only care about isSuccessful, the body (if any) must be closed.
                    // This is especially true for error responses which might have a body.
                    if (!response.isSuccessful) {
                        Log.w(
                            "ApiService",
                            "incrementView failed: ${response.code} - ${response.message}"
                        )
                        // Optionally, log response.body?.string() here if you want to see the error body
                    }
                    response.isSuccessful
                } // Response and body automatically closed
            } catch (e: Exception) {
                Log.e("ApiService", "Exception in incrementView", e)
                false
            }
        }

    suspend fun addWebsite(request: AddWebsiteRequest): AddWebsiteResult = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(request)
            val requestBody = json.toRequestBody(jsonMediaType)

            val httpRequest = Request.Builder()
                .url("$baseUrl/addWebsite")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

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