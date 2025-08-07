package com.example.discover.viewmodel

// import android.content.Context // No longer directly needed in constructor
import android.app.Application // Import Application
import androidx.lifecycle.AndroidViewModel // Import AndroidViewModel
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
// import android.widget.Toast // Remove this, ViewModel won't show Toasts directly
import com.example.discover.network.AddWebsiteResult
import kotlinx.coroutines.flow.update

// Enum to represent user interaction
enum class UserInteractionState {
    NONE, LIKED, DISLIKED
}

class DiscoverViewModel(
    application: Application // Change to Application
) : AndroidViewModel(application) { // Extend AndroidViewModel
    private companion object {
        private const val TAG = "DiscoverViewModel"
    }

    private val apiService = ApiService()
    // Use getApplication<Application>().applicationContext
    private val localStorage = LocalStorage(getApplication<Application>().applicationContext)

    // ... (all your existing StateFlows for UI data remain the same)
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

    // New StateFlow for Toast messages
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()


    private val visitedWebsites = mutableSetOf<String>()
    private val websiteHistory = mutableListOf<Website>()
    private var currentIndex = -1

    init {
        startWithFastestData()
        updateWebsitesInBackground()
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

    fun loadWebsites() {
        updateWebsitesInBackground()
    }

    private fun updateWebsitesInBackground() {
        viewModelScope.launch {
            _isUpdating.value = true
            val originalCurrentWebsite = _currentWebsite.value // Keep a reference to the original current website object

            try {
                val websitesList = apiService.getWebsites()
                if (websitesList.isNotEmpty()) {
                    // Update the main list of websites
                    _websites.value = websitesList
                    localStorage.saveWebsites(websitesList)
                    _error.value = null // Clear any previous error

                    // If there was a current website, try to find its updated version in the new list
                    // to refresh its data (e.g., likes/dislikes) but keep it as the current one.
                    val updatedCurrentWebsiteInstance = originalCurrentWebsite?.id?.let { currentId ->
                        websitesList.find { it.id == currentId }
                    }

                    if (updatedCurrentWebsiteInstance != null) {
                        // The current website still exists in the new list, update our local instance
                        // to reflect any changes from the server (e.g., updated like count).
                        _currentWebsite.value = updatedCurrentWebsiteInstance
                    } else if (originalCurrentWebsite != null) {
                        // The original current website is NOT in the new list.
                        // Per your requirement, we do nothing and keep 'originalCurrentWebsite'
                        // as '_currentWebsite.value'. It's already set.
                        // The user can continue interacting with this "stale" website data
                        // until they navigate away.
                        // '_websites.value' is updated, so 'next'/'random' will pick from the new list.
                    } else {
                        // There was no originalCurrentWebsite (it was null), so load a random one
                        // from the new list. This handles initial load or scenarios where no site was active.
                        // Also, reset history as we are picking a fresh start.
                        visitedWebsites.clear()
                        websiteHistory.clear()
                        currentIndex = -1
                        loadRandomWebsite()
                    }

                } else {
                    _error.value = "Using cached websites. API returned empty data."
                    // If API returned empty, _websites.value is unchanged (or from cache/static).
                    // _currentWebsite.value also remains as it was.
                    // If _currentWebsite was null and we have some websites, load one.
                    if (_currentWebsite.value == null && _websites.value.isNotEmpty()) {
                        loadRandomWebsite() // Potentially reset history here too if needed
                    }
                }
            } catch (e: Exception) {
                _error.value = "Using cached websites. Network error: ${e.message}"
                // On error, _websites.value is unchanged (or from cache/static).
                // _currentWebsite.value also remains as it was.
                // If _currentWebsite was null and we have some websites, load one.
                if (_currentWebsite.value == null && _websites.value.isNotEmpty()) {
                    loadRandomWebsite() // Potentially reset history here too if needed
                }
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
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentWebsite.update { current -> current?.copy(likes = current.likes - 1) }
                Log.d(TAG, "Website unliked: ${websiteToUpdate.name}")
            }
            UserInteractionState.DISLIKED -> {
                _currentUserInteractionState.value = UserInteractionState.LIKED
                _currentWebsite.update { current ->
                    current?.copy(likes = current.likes + 1, dislikes = current.dislikes - 1)
                }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.id, websiteToUpdate.url, "like")
                }
                Log.d(TAG, "Website changed from dislike to like: ${websiteToUpdate.name}")
            }
            UserInteractionState.NONE -> {
                _currentUserInteractionState.value = UserInteractionState.LIKED
                _currentWebsite.update { current -> current?.copy(likes = current.likes + 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.id, websiteToUpdate.url, "like")
                }
                Log.d(TAG, "Website liked: ${websiteToUpdate.name}")
            }
        }
    }

    fun dislikeWebsite() {
        val currentInteraction = _currentUserInteractionState.value
        val websiteToUpdate = currentWebsite.value ?: return

        when (currentInteraction) {
            UserInteractionState.DISLIKED -> {
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentWebsite.update { current -> current?.copy(dislikes = current.dislikes - 1) }
                Log.d(TAG, "Website undisliked: ${websiteToUpdate.name}")
            }
            UserInteractionState.LIKED -> {
                _currentUserInteractionState.value = UserInteractionState.DISLIKED
                _currentWebsite.update { current ->
                    current?.copy(dislikes = current.dislikes + 1, likes = current.likes - 1)
                }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.id, websiteToUpdate.url, "dislike")
                }
                Log.d(TAG, "Website changed from like to dislike: ${websiteToUpdate.name}")
            }
            UserInteractionState.NONE -> {
                _currentUserInteractionState.value = UserInteractionState.DISLIKED
                _currentWebsite.update { current -> current?.copy(dislikes = current.dislikes + 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.id, websiteToUpdate.url, "dislike")
                }
                Log.d(TAG, "Website disliked: ${websiteToUpdate.name}")
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
            val result = apiService.addWebsite(request)
            val message = when (result) {
                is AddWebsiteResult.Success -> {
                    hideAddWebsiteDialog() // Hide dialog on success
                    "Website added successfully!"
                }
                is AddWebsiteResult.Duplicate -> "This website already exists."
                is AddWebsiteResult.NetworkError -> "Network error. Please check your connection."
                is AddWebsiteResult.Error -> result.message
            }
            _toastMessage.value = message // Set the message for the UI to observe
        }
    }

    // Call this from the UI after the toast is shown
    fun toastMessageShown() {
        _toastMessage.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
