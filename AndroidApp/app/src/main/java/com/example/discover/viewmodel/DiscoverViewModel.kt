package com.example.discover.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discover.data.AddWebsiteRequest
import com.example.discover.data.LocalStorage
import com.example.discover.data.StaticWebsites
import com.example.discover.data.Website
import com.example.discover.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class DiscoverViewModel(
    private val context: Context
) : ViewModel() {
    // Define a TAG for this class
    private companion object {
        private const val TAG = "DiscoverViewModel"
    }

    private val apiService = ApiService()
    private val localStorage = LocalStorage(context)

    private val _websites = MutableStateFlow<List<Website>>(emptyList())
    val websites: StateFlow<List<Website>> = _websites.asStateFlow()

    private val _currentWebsite = MutableStateFlow<Website?>(null)
    val currentWebsite: StateFlow<Website?> = _currentWebsite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showAddWebsiteDialog = MutableStateFlow(false)
    val showAddWebsiteDialog: StateFlow<Boolean> = _showAddWebsiteDialog.asStateFlow()

    private val _showWebView = MutableStateFlow(false)
    val showWebView: StateFlow<Boolean> = _showWebView.asStateFlow()

    private val _currentWebViewUrl = MutableStateFlow<String?>(null)
    val currentWebViewUrl: StateFlow<String?> = _currentWebViewUrl.asStateFlow()

    private val visitedWebsites = mutableSetOf<String>()
    private val websiteHistory = mutableListOf<Website>()
    private var currentIndex = -1

    init {
        // Start immediately with fastest available data
        startWithFastestData()
        // Then update in background only if needed
        updateWebsitesInBackgroundIfNeeded()
    }

    private fun startWithFastestData() {
        // Priority order: Local cache → Static websites
        val cachedWebsites = localStorage.getCachedWebsites()

        if (cachedWebsites.isNotEmpty()) {
            // Use cached data for fastest startup (cache is kept forever)
            _websites.value = cachedWebsites
            _error.value = null
        } else {
            // Fallback to static websites
            _websites.value = StaticWebsites.websites
            _error.value = null
        }

        _isLoading.value = false

        // Load the first random website immediately
        loadRandomWebsite()
    }

    private fun updateWebsitesInBackgroundIfNeeded() {
        Log.d(TAG, "updateWebsitesInBackgroundIfNeeded")
        // Only update if cache is older than 12 hours
        if (!localStorage.shouldUpdateCache()) {
            Log.d(TAG, "Cache is up to date")
            return
        }

        viewModelScope.launch {
            // We can still set _isUpdating if you want to show a subtle loading indicator
            // for background updates. If not, you can remove these lines too.
            val wasUpdatingBefore = _isUpdating.value
            _isUpdating.value = true

            try {
                val newWebsitesList = apiService.getWebsites()

                if (newWebsitesList.isNotEmpty()) {
                    // Update with real API data and cache it
                    _websites.value = newWebsitesList
                    localStorage.saveWebsites(newWebsitesList)
                    _error.value = null // Clear any previous error related to fetching

                    // IMPORTANT: All state clearing and reloading logic has been removed.
                    // The UI will simply reflect the new list of websites when it next observes
                    // the `websites` StateFlow. The current website and history remain unchanged.

                    Log.d(TAG, "Background update successful")
                } else {
                    // Optionally, you might still want to log this or show a subtle, non-intrusive
                    // message if the API returns an empty list during a background refresh.
                    // For now, we'll just log it or do nothing to avoid user interruption.
                    // _error.value = "Background update: API returned empty data."
                    Log.d(TAG, "Background update: API returned empty data.")
                }
            } catch (e: Exception) {
                // Similarly, handle background errors without disrupting the user.
                // _error.value = "Background update error: ${e.message}"
                Log.d(TAG, "Background update error: ${e.message}")
            } finally {
                // Only revert _isUpdating if we were the ones to set it true for this specific operation
                if (!wasUpdatingBefore) {
                    _isUpdating.value = false
                    Log.d(TAG, "Background update finally: _isUpdating set to false")
                }
                Log.d(TAG, "Background update finally done")
            }
        }
    }

    fun loadWebsites() {
        // This function now triggers a manual refresh regardless of cache age
        updateWebsitesInBackground()
    }

    private fun updateWebsitesInBackground() {
        viewModelScope.launch {
            _isUpdating.value = true

            try {
                val websitesList = apiService.getWebsites()

                if (websitesList.isNotEmpty()) {
                    // Update with real API data and cache it
                    _websites.value = websitesList
                    localStorage.saveWebsites(websitesList)

                    // Clear any existing state and reload
                    _currentWebsite.value = null
                    visitedWebsites.clear()
                    websiteHistory.clear()
                    currentIndex = -1

                    // Load a new random website from updated data
                    loadRandomWebsite()

                    _error.value = null // Clear any previous error
                } else {
                    // Keep using current data, but show API unavailable message
                    _error.value = "Using cached websites. API returned empty data."
                }
            } catch (e: Exception) {
                // Keep using current data, but show network error
                _error.value = "Using cached websites. Network error: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun loadRandomWebsite() {
        val unvisitedWebsites = websites.value.filter { !visitedWebsites.contains(it.id) }

        if (unvisitedWebsites.isEmpty()) {
            // All websites visited, reset
            visitedWebsites.clear()
            websiteHistory.clear()
            currentIndex = -1

            // Try to load a random website again
            val allWebsites = websites.value
            if (allWebsites.isNotEmpty()) {
                val randomWebsite = allWebsites.random()
                loadWebsite(randomWebsite, addToHistory = true)
            } else {
                _error.value = "No websites available. Please try again."
            }
            return
        }

        val randomWebsite = unvisitedWebsites.random()
        loadWebsite(randomWebsite, addToHistory = true)
    }

    fun loadNextWebsite() {
        if (websiteHistory.isEmpty()) {
            loadRandomWebsite()
            return
        }

        if (currentIndex < websiteHistory.size - 1) {
            currentIndex++
            val website = websiteHistory[currentIndex]
            loadWebsite(website, addToHistory = false)
        } else {
            loadRandomWebsite()
        }
    }

    fun loadPreviousWebsite() {
        if (websiteHistory.isEmpty() || currentIndex <= 0) {
            return
        }

        currentIndex--
        val website = websiteHistory[currentIndex]
        loadWebsite(website, addToHistory = false)
    }

    private fun loadWebsite(website: Website, addToHistory: Boolean) {
        if (addToHistory) {
            if (currentIndex < websiteHistory.size - 1) {
                // Remove any forward history if we're going back and then to a new site
                val newSize = currentIndex + 1
                while (websiteHistory.size > newSize) {
                    websiteHistory.removeAt(websiteHistory.size - 1)
                }
            }
            websiteHistory.add(website)
            currentIndex = websiteHistory.size - 1
        }

        visitedWebsites.add(website.id)
        _currentWebsite.value = website

        // Track view (only if it's not a static website)
        if (!website.id.startsWith("website-")) {
            viewModelScope.launch {
                apiService.incrementView(website.id, website.url, "view")
            }
        }

        // Automatically open the website in WebView
        _currentWebViewUrl.value = website.url
        _showWebView.value = true
    }

    fun likeWebsite() {
        currentWebsite.value?.let { website ->
            // Only track likes for non-static websites
            if (!website.id.startsWith("website-")) {
                viewModelScope.launch {
                    apiService.incrementView(website.id, website.url, "like")
                }
            }
        }
    }

    fun dislikeWebsite() {
        currentWebsite.value?.let { website ->
            // Only track dislikes for non-static websites
            if (!website.id.startsWith("website-")) {
                viewModelScope.launch {
                    apiService.incrementView(website.id, website.url, "dislike")
                }
            }
        }
    }

    fun openWebsite() {
        currentWebsite.value?.let { website ->
            _currentWebViewUrl.value = website.url
            _showWebView.value = true
        }
    }

    fun closeWebView() {
        _showWebView.value = false
        _currentWebViewUrl.value = null
    }

    fun showAddWebsiteDialog() {
        _showAddWebsiteDialog.value = true
    }

    fun hideAddWebsiteDialog() {
        _showAddWebsiteDialog.value = false
    }

    fun addWebsite(name: String, url: String, description: String) {
        viewModelScope.launch {
            val request = AddWebsiteRequest(name, url, description)
            val success = apiService.addWebsite(request)

            if (success) {
                hideAddWebsiteDialog()
                loadWebsites() // Reload to include new website
            } else {
                _error.value = "Failed to add website"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 