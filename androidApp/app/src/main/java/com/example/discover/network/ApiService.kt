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

// Define possible outcomes for the addLink operation (keep your existing one)
sealed class AddLinkResult {
    object Success :
        AddLinkResult() // Consider passing the created/updated Link back: data class Success(val link: Link) : AddLinkResult()

    object Duplicate : AddLinkResult()
    data class Error(val message: String) : AddLinkResult()
    object NetworkError : AddLinkResult()
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

    suspend fun getLinks(): List<Link> = withContext(Dispatchers.IO) {
        val endpointUrl = "$baseUrl/getLinks?platform=mobile"
        Log.d(TAG, "Attempting to fetch links from: $endpointUrl")

        val request = Request.Builder().url(endpointUrl).get().build()

        try {
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "getLinks | Response code: ${response.code} for URL: ${request.url}")
                if (response.isSuccessful) {
                    val json = response.body.string() // Safely get body string
                    Log.d(TAG, "getLinks | Response body: $json")
                    // Using Array<Link>::class.java is correct for Gson with arrays
                    val downloadedArray: Array<Link> =
                        gson.fromJson(json, Array<Link>::class.java) ?: emptyArray()
                    Log.d(TAG, "getLinks | Parsed ${downloadedArray.size} links.")
                    return@use downloadedArray.toList()

                } else {
                    val errorBody = response.body.string() // Attempt to read error body for logging
                    Log.w(
                        TAG,
                        "getLinks | HTTP error: ${response.code} - ${response.message} for URL: ${request.url}. Body: $errorBody"
                    )
                    return@use emptyList() // Return empty list on HTTP error after retries
                }
            }
        } catch (e: IOException) {
            // This IOException is after all retries from RetryInterceptor have failed
            Log.e(
                TAG,
                "getLinks | IOException (Final after retries) for URL $endpointUrl: ${e.message}",
                e
            )
            return@withContext emptyList()
        } catch (e: Exception) {
            // Catch any other unexpected exceptions (e.g., JsonSyntaxException if API returns malformed JSON)
            Log.e(TAG, "getLinks | Generic exception for URL $endpointUrl: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    suspend fun incrementView(linkUrl: String?, action: String): Boolean =
        withContext(Dispatchers.IO) {
            Log.d(
                TAG,
                "incrementView | Called with url: '$linkUrl', action: '$action'"
            )
            if (linkUrl == null) {
                Log.e(TAG, "incrementView | Failed: linkUrl is null")
                // Consider if you need a full stack trace here for a simple null check
                // Log.d("myapp", Log.getStackTraceString(java.lang.Exception("Link URL is null trace")))
                return@withContext false
            }

            val encodedUrl = try {
                java.net.URLEncoder.encode(linkUrl, "UTF-8")
            } catch (e: Exception) {
                Log.e(TAG, "incrementView | Failed to URL encode: $linkUrl", e)
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

    suspend fun addLink(linkData: Link): AddLinkResult = withContext(Dispatchers.IO) {
        val endpointUrl = "$baseUrl/addLink"
        Log.d(TAG, "addLink | Attempting to add link to: $endpointUrl with data: $linkData")

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
                    TAG, "addLink | Response code: ${response.code} for URL: ${httpRequest.url}"
                )
                return@use when {
                    response.isSuccessful -> { // Typically 200, 201, 204
                        Log.i(
                            TAG, "addLink | Successfully added link. Code: ${response.code}"
                        )
                        AddLinkResult.Success // Or parse response body if API returns the created object
                    }

                    response.code == 409 -> { // HTTP 409 Conflict
                        Log.w(TAG, "addLink | Duplicate entry (409) for URL: ${httpRequest.url}")
                        AddLinkResult.Duplicate
                    }

                    else -> {
                        val errorBody = response.body.string()
                        Log.w(
                            TAG,
                            "addLink | Failed: ${response.code} - ${response.message} for URL: ${httpRequest.url}. Body: $errorBody"
                        )
                        AddLinkResult.Error("Failed to add link (${response.code}): ${response.message}")
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(
                TAG,
                "addLink | Network exception (Final after retries) for URL $endpointUrl: ${e.message}",
                e
            )
            return@withContext AddLinkResult.NetworkError
        } catch (e: Exception) {
            Log.e(TAG, "addLink | Generic exception for URL $endpointUrl: ${e.message}", e)
            return@withContext AddLinkResult.Error("An unexpected error occurred: ${e.message}")
        }
    }
}
