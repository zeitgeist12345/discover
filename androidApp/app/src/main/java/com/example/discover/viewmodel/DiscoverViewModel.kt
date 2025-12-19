package com.example.discover.viewmodel

// import android.content.Context // No longer directly needed in constructor
// import android.widget.Toast // Remove this, ViewModel won't show Toasts directly
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.discover.data.Link
import com.example.discover.data.StaticWebsites
import com.example.discover.network.AddWebsiteResult
import com.example.discover.network.ApiService
import com.example.discover.utils.TimeStats
import com.example.discover.utils.TimeTrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    // ... (all your existing StateFlows for UI data remain the same)
    private val _websites = MutableStateFlow<List<Link>>(emptyList())
    val websites: StateFlow<List<Link>> = _websites.asStateFlow()

    private val _currentWebsite = MutableStateFlow<Link?>(null)
    val currentWebsite: StateFlow<Link?> = _currentWebsite.asStateFlow()

    private val _showAddWebsiteDialog = MutableStateFlow(false)
    val showAddWebsiteDialog: StateFlow<Boolean> = _showAddWebsiteDialog.asStateFlow()

    private val _showWebView = MutableStateFlow(true)
    val showWebView: StateFlow<Boolean> = _showWebView.asStateFlow()

    private val _isWebViewLoading = MutableStateFlow(true)
    val isWebViewLoading: StateFlow<Boolean> = _isWebViewLoading.asStateFlow()

    private val _currentWebViewUrl = MutableStateFlow<String?>(null)
    val currentWebViewUrl: StateFlow<String?> = _currentWebViewUrl.asStateFlow()

    private val _currentUserInteractionState = MutableStateFlow(UserInteractionState.NONE)
    val currentUserInteractionState: StateFlow<UserInteractionState> =
        _currentUserInteractionState.asStateFlow()
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    private val _isApiAvailable = MutableStateFlow(0) // Default to true or false as you see fit
    val isApiAvailable: StateFlow<Int> = _isApiAvailable.asStateFlow()
    private val visitedWebsites = mutableSetOf<String>()
    private val websiteHistory = mutableListOf<Link>()
    private var currentIndex = -1
    private val timeTrackingManager = TimeTrackingManager(application)
    private val _timeStats = MutableStateFlow(TimeStats(0, 0, 0, 0, 0))
    val timeStats: StateFlow<TimeStats> = _timeStats.asStateFlow()

    init {
        startWithFastestData()
        loadTimeStats()
    }

    // Call when app becomes visible
    fun onAppForeground() {
        timeTrackingManager.startSession()
    }

    // Call when app goes to background
    fun onAppBackground() {
        timeTrackingManager.endSession()
        loadTimeStats() // Refresh stats
    }

    fun loadTimeStats() {
        viewModelScope.launch {
            _timeStats.value = timeTrackingManager.getTimeStats()
        }
    }

    private fun startWithFastestData() {
        Log.d(TAG, "Begin Start with fastest data")
        Log.d(TAG, "Websites: ${_websites.value.size}")
        Log.d(TAG, "Static websites: ${StaticWebsites.websites.size}")
        if (_websites.value.isEmpty()) {
            Log.d(TAG, "Starting with static websites")
            _websites.value = StaticWebsites.websites
        }
        Log.d(TAG, "End Start with fastest data")
    }

    private fun updateWebsitesInBackground() {
        viewModelScope.launch {
            try {
                val websitesList = apiService.getWebsites()
                if (websitesList.isNotEmpty()) {
                    _isApiAvailable.value = 1
                    Log.d(
                        TAG,
                        "API returned ${websitesList.size} items. First item from API before assigning: URL=${websitesList.firstOrNull()?.url}, Name=${websitesList.firstOrNull()?.name}"
                    )
                    // Update the main list of websites
                    _websites.value = websitesList
                    Log.d(
                        TAG,
                        "_websites.value updated. Size: ${_websites.value.size}. First item URL: ${_websites.value.firstOrNull()?.url}, Name: ${_websites.value.firstOrNull()?.name}"
                    )
                } else {
                    _isApiAvailable.value = -1
                }
            } catch (e: Exception) {
                _isApiAvailable.value = -1
                e.printStackTrace()
            }
        }
    }

    fun loadRandomWebsite() {
        Log.d(TAG, "Start Loading random website")
        val unvisitedWebsites = websites.value.filter { !visitedWebsites.contains(it.url) }
        if (unvisitedWebsites.isEmpty()) {
            visitedWebsites.clear()
            websiteHistory.clear()
            currentIndex = -1
            val allWebsites = websites.value
            if (allWebsites.isNotEmpty()) {
                val randomWebsite = allWebsites.random()
                loadWebsite(randomWebsite, addToHistory = true)
            } else {
                Log.d(TAG, "No websites available. Please try again.")
            }
            return
        }
        val randomWebsite = unvisitedWebsites.random()

        Log.d(TAG, "Random website: ${randomWebsite.name}")
        loadWebsite(randomWebsite, addToHistory = true)
        Log.d(TAG, "End Loading random website")
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

    fun updateNavigatedPreviousWebsite() {
        if (websiteHistory.isEmpty() || currentIndex < 0) {
            return
        }
        currentIndex--
    }

    fun loadPreviousWebsite() {
        if (websiteHistory.isEmpty() || currentIndex <= 0) {
            return
        }
        currentIndex--
        val website = websiteHistory[currentIndex]
        loadWebsite(website, addToHistory = false)
    }

    private fun loadWebsite(website: Link, addToHistory: Boolean) {
        _currentUserInteractionState.value =
            UserInteractionState.NONE // Reset interaction state for new website

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
        visitedWebsites.add(website.url)
        _currentWebsite.value = website
        viewModelScope.launch {
            apiService.incrementView(website.url, "view")
        }
        _currentWebViewUrl.value = website.url
        _showWebView.value = true
    }

    fun onWebViewPageVisible() {
        _isWebViewLoading.value = false
        updateWebsitesInBackground()
    }

    fun likeWebsite() {
        val currentInteraction = _currentUserInteractionState.value
        val websiteToUpdate = currentWebsite.value ?: return

        when (currentInteraction) {
            UserInteractionState.LIKED -> {
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentWebsite.update { current -> current?.copy(likesMobile = current.likesMobile - 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.url, "unlikes")
                }
                Log.d(TAG, "Website unliked: ${websiteToUpdate.name}")
            }

            UserInteractionState.DISLIKED -> {
                // Undislike
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentWebsite.update { current -> current?.copy(dislikesMobile = current.dislikesMobile - 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.url, "undislikes")
                }
                Log.d(TAG, "Website undisliked: ${websiteToUpdate.name}")

                // Like
                _currentUserInteractionState.value = UserInteractionState.LIKED
                _currentWebsite.update { current -> current?.copy(likesMobile = current.likesMobile + 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.url, "likes")
                }
                Log.d(TAG, "Website liked: ${websiteToUpdate.name}")
            }

            UserInteractionState.NONE -> {
                _currentUserInteractionState.value = UserInteractionState.LIKED
                _currentWebsite.update { current -> current?.copy(likesMobile = current.likesMobile + 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.url, "likes")
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
                _currentWebsite.update { current -> current?.copy(dislikesMobile = current.dislikesMobile - 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.url, "undislikes")
                }
                Log.d(TAG, "Website undisliked: ${websiteToUpdate.name}")
            }

            UserInteractionState.LIKED -> {
                // Unlike
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentWebsite.update { current -> current?.copy(likesMobile = current.likesMobile - 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.url, "unlikes")
                }
                Log.d(TAG, "Website unliked: ${websiteToUpdate.name}")

                // DisLike
                _currentUserInteractionState.value = UserInteractionState.DISLIKED
                _currentWebsite.update { current -> current?.copy(dislikesMobile = current.dislikesMobile + 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.url, "dislikes")
                }
                Log.d(TAG, "Website disliked: ${websiteToUpdate.name}")
            }

            UserInteractionState.NONE -> {
                _currentUserInteractionState.value = UserInteractionState.DISLIKED
                _currentWebsite.update { current -> current?.copy(dislikesMobile = current.dislikesMobile + 1) }
                viewModelScope.launch {
                    apiService.incrementView(websiteToUpdate.url, "dislikes")
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
        // FIX: Set the current website to the one we were just viewing.
        _currentWebsite.value = websiteHistory.getOrNull(currentIndex)
        _currentWebViewUrl.value = null
    }

    fun showAddWebsiteDialog() {
        _showAddWebsiteDialog.value = true
    }

    fun hideAddWebsiteDialog() {
        _showAddWebsiteDialog.value = false
    }

    fun addWebsite(name: String, url: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            val request = Link(name = name, url = url, description = description, tags = tags)
            val result = apiService.addWebsite(request)
            val message = when (result) {
                is AddWebsiteResult.Success -> {
                    hideAddWebsiteDialog() // Hide dialog on success
                    "Link submitted for spam review successfully! The link will be live globally after review approval 🎉"
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
}
