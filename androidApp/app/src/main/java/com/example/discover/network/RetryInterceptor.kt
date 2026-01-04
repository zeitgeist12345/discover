/* Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

package com.example.discover.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptor(
    private val initialDelayMillis: Long = 1500, // Initial delay 1.5 seconds
    private val factor: Double = 2.0 // Unlimited retries with exponential backoff to avoid transient errors.
) : Interceptor {

    private companion object {
        private const val TAG = "RetryInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var tryCount = 0
        var currentDelay = initialDelayMillis

        while (response == null) {
            try {
                Log.d(TAG, "Attempt ${tryCount + 1} for request: ${request.method} ${request.url}")
                response = chain.proceed(request) // Attempt the request

                // Check for HTTP codes that we might want to retry
                if (!response.isSuccessful && isRetryableHttpCode(response.code)) {
                    val responseBodyString = response.body.string() // Consume body to close
                    Log.w(
                        TAG,
                        "HTTP Error: ${response.code} for ${request.url}. Attempt ${tryCount + 1}. Body: $responseBodyString. Retrying in ${currentDelay}ms."
                    )
                    response.close() // IMPORTANT: Close the previous unsuccessful response
                    response = null // Signal to retry
                } else if (!response.isSuccessful) {
                    // Non-retryable HTTP error, log and break to return this response
                    Log.w(
                        TAG,
                        "Non-retryable HTTP Error: ${response.code} for ${request.url} on attempt ${tryCount + 1}."
                    )
                    break // Exit loop, will return this unsuccessful response
                } else {
                    Log.d(TAG, "Request successful for ${request.url} on attempt ${tryCount + 1}")
                    // Successful response, loop will terminate
                }

            } catch (e: IOException) {
                Log.w(
                    TAG,
                    "Request failed for ${request.url} (Attempt ${tryCount + 1}: ${e.message}. Retrying in ${currentDelay}ms."
                )
            }

            if (response == null) { // If request failed (due to IOException or marked for retry)
                tryCount++
                try {
                    Thread.sleep(currentDelay)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    Log.w(TAG, "Retry interrupted for ${request.url}", ie)
                    throw IOException(
                        "Retry interrupted for ${request.url}", ie
                    ) // Re-throw if interrupted
                }
                currentDelay = (currentDelay * factor).toLong()
                    .coerceAtMost(TimeUnit.MINUTES.toMillis(1)) // Cap delay

            }
        }

        // If all retries failed and we have an exception, throw it

        // Return the successful response, or the non-retryable error response,
        // or throw an exception if all retries failed.
        return response ?: throw (IOException("Failed to get a response for ${request.url}."))
    }

    private fun isRetryableHttpCode(code: Int): Boolean {
        // Customize this list based on your API's behavior
        // Example: Retry on Server Errors (5xx), Timeout (408), Too Many Requests (429)
        return code in 500..599 || code == 408 || code == 429
    }
}
