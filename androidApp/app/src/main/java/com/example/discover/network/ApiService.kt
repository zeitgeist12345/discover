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
import java.util.concurrent.TimeUnit

// Define possible outcomes for the addWebsite operation (keep your existing one)
sealed class AddWebsiteResult {
    object Success :
        AddWebsiteResult() // Consider passing the created/updated Link back: data class Success(val link: Link) : AddWebsiteResult()

    object Duplicate : AddWebsiteResult()
    data class Error(val message: String) : AddWebsiteResult()
    object NetworkError : AddWebsiteResult()
}

class ApiService {
    private companion object {
        private const val TAG = "ApiService"
        private const val RETRY_INITIAL_DELAY_MS = 1500L
    }

    // Configure OkHttpClient with the RetryInterceptor and logging
    private val client: OkHttpClient by lazy {
        val builder =
            OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS) // Standard timeouts
                .readTimeout(20, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(
                    RetryInterceptor(
                        initialDelayMillis = RETRY_INITIAL_DELAY_MS, factor = 2.0
                    )
                ) // Add our retry interceptor
        builder.build()
    }

    private val gson = Gson()
    private val baseUrl = "https://backend.discoverall.space"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType() // Specify charset

    suspend fun getWebsites(): List<Link> = withContext(Dispatchers.IO) {
        val endpointUrl = "$baseUrl/getWebsites?platform=mobile"
        Log.d(TAG, "Attempting to fetch websites from: $endpointUrl")

        val request = Request.Builder().url(endpointUrl).get().build()

        try {
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "getWebsites | Response code: ${response.code} for URL: ${request.url}")
                if (response.isSuccessful) {
                    val json = response.body.string() // Safely get body string
                    Log.d(TAG, "getWebsites | Response body: $json")
                    // Using Array<Link>::class.java is correct for Gson with arrays
                    val downloadedArray: Array<Link> =
                        gson.fromJson(json, Array<Link>::class.java) ?: emptyArray()
                    Log.d(TAG, "getWebsites | Parsed ${downloadedArray.size} websites.")
                    return@use downloadedArray.toList()

                } else {
                    val errorBody = response.body.string() // Attempt to read error body for logging
                    Log.w(
                        TAG,
                        "getWebsites | HTTP error: ${response.code} - ${response.message} for URL: ${request.url}. Body: $errorBody"
                    )
                    return@use emptyList() // Return empty list on HTTP error after retries
                }
            }
        } catch (e: IOException) {
            // This IOException is after all retries from RetryInterceptor have failed
            Log.e(
                TAG,
                "getWebsites | IOException (Final after retries) for URL $endpointUrl: ${e.message}",
                e
            )
            return@withContext emptyList()
        } catch (e: Exception) {
            // Catch any other unexpected exceptions (e.g., JsonSyntaxException if API returns malformed JSON)
            Log.e(TAG, "getWebsites | Generic exception for URL $endpointUrl: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    suspend fun incrementView(websiteUrl: String?, action: String): Boolean =
        withContext(Dispatchers.IO) {
            Log.d(
                TAG,
                "incrementView | Called with url: '$websiteUrl', action: '$action'"
            )
            if (websiteUrl == null) {
                Log.e(TAG, "incrementView | Failed: websiteUrl is null")
                // Consider if you need a full stack trace here for a simple null check
                // Log.d("myapp", Log.getStackTraceString(java.lang.Exception("Website URL is null trace")))
                return@withContext false
            }

            val encodedUrl = try {
                java.net.URLEncoder.encode(websiteUrl, "UTF-8")
            } catch (e: Exception) {
                Log.e(TAG, "incrementView | Failed to URL encode: $websiteUrl", e)
                return@withContext false
            }

            val fullUrl = "$baseUrl/incrementView?url=$encodedUrl&action=$action"
            Log.d(TAG, "incrementView | Attempting POST to: $fullUrl")

            val request = Request.Builder().url(fullUrl)
                .post("".toRequestBody(null)) // Empty POST body, OkHttp requires a non-null RequestBody for POST
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    Log.d(
                        TAG,
                        "incrementView | Response code: ${response.code} for URL: ${request.url}"
                    )
                    if (!response.isSuccessful) {
                        val errorBody = response.body.string()
                        Log.w(
                            TAG,
                            "incrementView | Failed: ${response.code} - ${response.message} for URL: ${request.url}. Body: $errorBody"
                        )
                    }
                    return@use response.isSuccessful // Return true if 2xx, false otherwise (after retries)
                }
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "incrementView | IOException (Final after retries) for URL $fullUrl: ${e.message}",
                    e
                )
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "incrementView | Generic exception for URL $fullUrl: ${e.message}", e)
                return@withContext false
            }
        }

    suspend fun addWebsite(linkData: Link): AddWebsiteResult = withContext(Dispatchers.IO) {
        val endpointUrl = "$baseUrl/addWebsite"
        Log.d(TAG, "addWebsite | Attempting to add website to: $endpointUrl with data: $linkData")

        Log.d("ApiService", "Incoming JSON: $linkData")
        val json = gson.toJson(linkData)
        Log.d("ApiService", "Outgoing JSON: $json")
        val requestBody = json.toRequestBody(jsonMediaType)

        val httpRequest = Request.Builder().url(endpointUrl).post(requestBody)
            // .addHeader("Content-Type", "application/json") // toRequestBody with MediaType sets this
            .build()

        try {
            client.newCall(httpRequest).execute().use { response ->
                Log.d(
                    TAG, "addWebsite | Response code: ${response.code} for URL: ${httpRequest.url}"
                )
                return@use when {
                    response.isSuccessful -> { // Typically 200, 201, 204
                        Log.i(
                            TAG, "addWebsite | Successfully added website. Code: ${response.code}"
                        )
                        AddWebsiteResult.Success // Or parse response body if API returns the created object
                    }

                    response.code == 409 -> { // HTTP 409 Conflict
                        Log.w(TAG, "addWebsite | Duplicate entry (409) for URL: ${httpRequest.url}")
                        AddWebsiteResult.Duplicate
                    }

                    else -> {
                        val errorBody = response.body.string()
                        Log.w(
                            TAG,
                            "addWebsite | Failed: ${response.code} - ${response.message} for URL: ${httpRequest.url}. Body: $errorBody"
                        )
                        AddWebsiteResult.Error("Failed to add website (${response.code}): ${response.message}")
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(
                TAG,
                "addWebsite | Network exception (Final after retries) for URL $endpointUrl: ${e.message}",
                e
            )
            return@withContext AddWebsiteResult.NetworkError
        } catch (e: Exception) {
            Log.e(TAG, "addWebsite | Generic exception for URL $endpointUrl: ${e.message}", e)
            return@withContext AddWebsiteResult.Error("An unexpected error occurred: ${e.message}")
        }
    }
}
