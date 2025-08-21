package com.example.discover.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptor(
    private val maxRetries: Int = 10,
    private val initialDelayMillis: Long = 1500, // Initial delay 1.5 seconds
    private val factor: Double = 2.0
) : Interceptor {

    private companion object {
        private const val TAG = "RetryInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null // Store the last exception
        var tryCount = 0
        var currentDelay = initialDelayMillis

        while (tryCount < maxRetries && response == null) {
            try {
                Log.d(TAG, "Attempt ${tryCount + 1} for request: ${request.method} ${request.url}")
                response = chain.proceed(request) // Attempt the request

                // Check for HTTP codes that we might want to retry
                if (!response.isSuccessful && isRetryableHttpCode(response.code)) {
                    val responseBodyString = response.body.string() // Consume body to close
                    Log.w(
                        TAG,
                        "HTTP Error: ${response.code} for ${request.url}. Attempt ${tryCount + 1} of $maxRetries. Body: $responseBodyString. Retrying in ${currentDelay}ms."
                    )
                    response.close() // IMPORTANT: Close the previous unsuccessful response
                    exception = IOException("Retryable HTTP error: ${response.code}") // Store a synthetic exception
                    response = null // Signal to retry
                } else if (!response.isSuccessful) {
                    // Non-retryable HTTP error, log and break to return this response
                    Log.w(TAG, "Non-retryable HTTP Error: ${response.code} for ${request.url} on attempt ${tryCount + 1}.")
                    break // Exit loop, will return this unsuccessful response
                } else {
                    Log.d(TAG, "Request successful for ${request.url} on attempt ${tryCount + 1}")
                    // Successful response, loop will terminate
                }

            } catch (e: IOException) {
                exception = e // Store the actual IO exception
                Log.w(
                    TAG,
                    "Request failed for ${request.url} (Attempt ${tryCount + 1}/$maxRetries): ${e.message}. Retrying in ${currentDelay}ms."
                )
                // response remains null, loop will continue if tryCount < maxRetries
            }

            if (response == null) { // If request failed (due to IOException or marked for retry)
                tryCount++
                if (tryCount < maxRetries) {
                    try {
                        Thread.sleep(currentDelay)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        Log.w(TAG, "Retry interrupted for ${request.url}", ie)
                        throw IOException("Retry interrupted for ${request.url}", ie) // Re-throw if interrupted
                    }
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(TimeUnit.MINUTES.toMillis(1)) // Cap delay
                }
            }
        }

        // If all retries failed and we have an exception, throw it
        if (response == null && exception != null) {
            Log.e(TAG, "All $maxRetries retries failed for ${request.url}. Last exception: ${exception.message}")
            throw exception
        }

        // Return the successful response, or the non-retryable error response,
        // or throw an exception if all retries failed.
        return response ?: throw (IOException("Failed to get a response after $maxRetries retries for ${request.url}."))
    }

    private fun isRetryableHttpCode(code: Int): Boolean {
        // Customize this list based on your API's behavior
        // Example: Retry on Server Errors (5xx), Timeout (408), Too Many Requests (429)
        return code in 500..599 || code == 408 || code == 429
    }
}
