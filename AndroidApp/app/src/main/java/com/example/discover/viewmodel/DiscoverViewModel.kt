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
import android.widget.Toast
import com.example.discover.network.AddWebsiteResult
import kotlinx.coroutines.flow.update

// Enum to represent user interaction
enum class UserInteractionState {
    NONE, LIKED, DISLIKED
}

class DiscoverViewModel(
    private val context: Context
) : ViewModel() {
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

    private val _currentUserInteractionState = MutableStateFlow(UserInteractionState.NONE)
    val currentUserInteractionState: StateFlow<UserInteractionState> = _currentUserInteractionState.asStateFlow()

    private val visitedWebsites = mutableSetOf<String>()
    private val websiteHistory = mutableListOf<Website>()
    private var currentIndex = -1

    init {
        startWithFastestData()
        updateWebsitesInBackgroundIfNeeded()
    }

    private fun startWithFastestData() {
        val cachedWebsites = localStorage.getCachedWebsites()
        if (cachedWebsites.isNotEmpty()) {
            _websites.value = cachedWebsites
        } else {
            _websites.value = StaticWebsites.websites
        }
        _isLoading.value = false
        loadRandomWebsite()
    }

    private fun updateWebsitesInBackgroundIfNeeded() {
        Log.d(TAG, "updateWebsitesInBackgroundIfNeeded")
        if (!localStorage.shouldUpdateCache()) {
            Log.d(TAG, "Cache is up to date")
            return
        }
        viewModelScope.launch {
            val wasUpdatingBefore = _isUpdating.value
            _isUpdating.value = true
            try {
                val newWebsitesList = apiService.getWebsites()
                if (newWebsitesList.isNotEmpty()) {
                    _websites.value = newWebsitesList
                    localStorage.saveWebsites(newWebsitesList)
                    _error.value = null
                    Log.d(TAG, "Background update successful")
                } else {
                    Log.d(TAG, "Background update: API returned empty data.")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Background update error: ${e.message}")
            } finally {
                if (!wasUpdatingBefore) {
                    _isUpdating.value = false
                    Log.d(TAG, "Background update finally: _isUpdating set to false")
                }
                Log.d(TAG, "Background update finally done")
            }
        }
    }

    fun loadWebsites() {
        updateWebsitesInBackground()
    }

    private fun updateWebsitesInBackground() {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val websitesList = apiService.getWebsites()
                if (websitesList.isNotEmpty()) {
                    _websites.value = websitesList
                    localStorage.saveWebsites(websitesList)
                    _currentWebsite.value = null
                    visitedWebsites.clear()
                    websiteHistory.clear()
                    currentIndex = -1
                    loadRandomWebsite() // This will also reset interaction state
                    _error.value = null
                } else {
                    _error.value = "Using cached websites. API returned empty data."
                }
            } catch (e: Exception) {
                _error.value = "Using cached websites. Network error: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun loadRandomWebsite() {
        val unvisitedWebsites = websites.value.filter { !visitedWebsites.contains(it.id) }
        if (unvisitedWebsites.isEmpty()) {
            visitedWebsites.clear()
            websiteHistory.clear()
            currentIndex = -1
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
        _currentUserInteractionState.value = UserInteractionState.NONE // Reset interaction state for new website

        if (addToHistory) {
            if (currentIndex < websiteHistory.size - 1) {
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
        viewModelScope.launch {
            apiService.incrementView(website.id, website.url, "view")
        }
        _currentWebViewUrl.value = website.url
        _showWebView.value = true
    }

    fun likeWebsite() {
        val currentInteraction = _currentUserInteractionState.value
        val websiteToUpdate = currentWebsite.value ?: return

        when (currentInteraction) {
            UserInteractionState.LIKED -> {
                // Currently liked, so unlike it
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentWebsite.update { current -> current?.copy(likes = current.likes - 1) } // Decrement like
                viewModelScope.launch {
                    // Consider if you need a "unlike" endpoint or if incrementing like with -1 works
                    // For now, let's assume you might not send an API call for "unliking"
                    // or you'd have a specific decrement endpoint.
                    // apiService.decrementView(websiteToUpdate.id, websiteToUpdate.url, "like")
                    Log.d(TAG, "Website unliked: ${websiteToUpdate.name}")
                }
            }
            UserInteractionState.DISLIKED -> {
                // Currently disliked, so change to liked
                _currentUserInteractionState.value = UserInteractionState.LIKED
                _currentWebsite.update { current ->
                    current?.copy(
                        likes = current.likes + 1, // Increment like
                        dislikes = current.dislikes - 1 // Decrement dislike
                    )
                }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.id, websiteToUpdate.url, "like")
                    // apiService.decrementView(websiteToUpdate.id, websiteToUpdate.url, "dislike") // If you track decrements
                    Log.d(TAG, "Website changed from dislike to like: ${websiteToUpdate.name}")
                }
            }
            UserInteractionState.NONE -> {
                // Currently neutral, so like it
                _currentUserInteractionState.value = UserInteractionState.LIKED
                _currentWebsite.update { current -> current?.copy(likes = current.likes + 1) } // Increment like
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.id, websiteToUpdate.url, "like")
                    Log.d(TAG, "Website liked: ${websiteToUpdate.name}")
                }
            }
        }
    }

    fun dislikeWebsite() {
        val currentInteraction = _currentUserInteractionState.value
        val websiteToUpdate = currentWebsite.value ?: return

        when (currentInteraction) {
            UserInteractionState.DISLIKED -> {
                // Currently disliked, so undislike it
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentWebsite.update { current -> current?.copy(dislikes = current.dislikes - 1) } // Decrement dislike
                viewModelScope.launch {
                    // Similar to unlike, consider API for "undisliking"
                    // apiService.decrementView(websiteToUpdate.id, websiteToUpdate.url, "dislike")
                    Log.d(TAG, "Website undisliked: ${websiteToUpdate.name}")
                }
            }
            UserInteractionState.LIKED -> {
                // Currently liked, so change to disliked
                _currentUserInteractionState.value = UserInteractionState.DISLIKED
                _currentWebsite.update { current ->
                    current?.copy(
                        dislikes = current.dislikes + 1, // Increment dislike
                        likes = current.likes - 1 // Decrement like
                    )
                }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.id, websiteToUpdate.url, "dislike")
                    // apiService.decrementView(websiteToUpdate.id, websiteToUpdate.url, "like") // If you track decrements
                    Log.d(TAG, "Website changed from like to dislike: ${websiteToUpdate.name}")
                }
            }
            UserInteractionState.NONE -> {
                // Currently neutral, so dislike it
                _currentUserInteractionState.value = UserInteractionState.DISLIKED
                _currentWebsite.update { current -> current?.copy(dislikes = current.dislikes + 1) } // Increment dislike
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.id, websiteToUpdate.url, "dislike")
                    Log.d(TAG, "Website disliked: ${websiteToUpdate.name}")
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
        // _currentUserInteractionState.value = UserInteractionState.NONE // Optional: reset on close
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
            val result = apiService.addWebsite(request)
            var text = "-"
            val duration = Toast.LENGTH_SHORT
            when (result) {
                is AddWebsiteResult.Success -> {
                    text = "Website added successfully!"
                    hideAddWebsiteDialog()
                }
                is AddWebsiteResult.Duplicate -> text = "This website already exists."
                is AddWebsiteResult.NetworkError -> text = "Network error. Please check your connection."
                is AddWebsiteResult.Error -> text = result.message
            }
            val toast = Toast.makeText(context, text, duration)
            toast.show()
        }
    }

    fun clearError() {
        _error.value = null
    }
}
