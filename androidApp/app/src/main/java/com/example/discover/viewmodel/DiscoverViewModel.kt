package com.example.discover.viewmodel

// import android.content.Context // No longer directly needed in constructor
// import android.widget.Toast // Remove this, ViewModel won't show Toasts directly
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.discover.data.Link
import com.example.discover.data.StaticLinks
import com.example.discover.network.AddLinkResult
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
    private val _links = MutableStateFlow<List<Link>>(emptyList())
    val links: StateFlow<List<Link>> = _links.asStateFlow()

    private val _currentLink = MutableStateFlow<Link?>(null)
    val currentLink: StateFlow<Link?> = _currentLink.asStateFlow()

    private val _showAddLinkDialog = MutableStateFlow(false)
    val showAddLinkDialog: StateFlow<Boolean> = _showAddLinkDialog.asStateFlow()

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
    private val visitedLinks = mutableSetOf<String>()
    private val linkHistory = mutableListOf<Link>()
    private var currentIndex = -1
    private val timeTrackingManager = TimeTrackingManager(application)
    private val _timeStats = MutableStateFlow(TimeStats(0, 0, 0, 0, 0, 0))
    val timeStats: StateFlow<TimeStats> = _timeStats.asStateFlow()

    init {
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
        _timeStats.value = timeTrackingManager.getTimeStats()
    }

    private fun startWithFastestData() {
        Log.d(TAG, "Begin Start with fastest data")
        Log.d(TAG, "Links: ${_links.value.size}")
        Log.d(TAG, "Static links: ${StaticLinks.links.size}")
        if (_links.value.isEmpty()) {
            Log.d(TAG, "Starting with static links")
            _links.value = StaticLinks.links
        }
        Log.d(TAG, "End Start with fastest data")
    }

    private fun updateLinksInBackground() {
        viewModelScope.launch {
            try {
                val linksList = apiService.getLinks()
                if (linksList.isNotEmpty()) {
                    _isApiAvailable.value = 1
                    Log.d(
                        TAG,
                        "API returned ${linksList.size} items. First item from API before assigning: URL=${linksList.firstOrNull()?.url}, Name=${linksList.firstOrNull()?.name}"
                    )
                    // Update the main list of links
                    _links.value = linksList
                    Log.d(
                        TAG,
                        "_links.value updated. Size: ${_links.value.size}. First item URL: ${_links.value.firstOrNull()?.url}, Name: ${_links.value.firstOrNull()?.name}"
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

    fun loadRandomLink() {
        Log.d(TAG, "Start Loading random link")
        val unvisitedLinks = links.value.filter { !visitedLinks.contains(it.url) }
        if (unvisitedLinks.isEmpty()) {
            visitedLinks.clear()
            linkHistory.clear()
            currentIndex = -1
            val allLinks = links.value
            if (allLinks.isNotEmpty()) {
                val randomLink = allLinks.random()
                loadLink(randomLink, addToHistory = true)
            } else {
                Log.d(TAG, "No links available. Please try again.")
            }
            return
        }
        val randomLink = unvisitedLinks.random()

        Log.d(TAG, "Random link: ${randomLink.name}")
        loadLink(randomLink, addToHistory = true)
        Log.d(TAG, "End Loading random link")
    }

    fun loadNextLink() {
        if (linkHistory.isEmpty()) {
            loadRandomLink()
            return
        }
        if (currentIndex < linkHistory.size - 1) {
            currentIndex++
            val link = linkHistory[currentIndex]
            loadLink(link, addToHistory = false)
        } else {
            loadRandomLink()
        }
    }

    fun updateNavigatedPreviousLink() {
        if (linkHistory.isEmpty() || currentIndex < 0) {
            return
        }
        currentIndex--
    }

    fun loadPreviousLink() {
        if (linkHistory.isEmpty() || currentIndex <= 0) {
            return
        }
        currentIndex--
        val link = linkHistory[currentIndex]
        loadLink(link, addToHistory = false)
    }

    private fun loadLink(link: Link, addToHistory: Boolean) {
        _currentUserInteractionState.value =
            UserInteractionState.NONE // Reset interaction state for new link

        if (addToHistory) {
            if (currentIndex < linkHistory.size - 1) {
                val newSize = currentIndex + 1
                while (linkHistory.size > newSize) {
                    linkHistory.removeAt(linkHistory.size - 1)
                }
            }
            linkHistory.add(link)
            currentIndex = linkHistory.size - 1
        }
        visitedLinks.add(link.url)
        _currentLink.value = link
        viewModelScope.launch {
            apiService.incrementView(link.url, "view")
        }
        _currentWebViewUrl.value = link.url
        _showWebView.value = true
    }

    fun onWebViewPageVisible() {
        _isWebViewLoading.value = false
        startWithFastestData()
        updateLinksInBackground()
    }

    fun likeLink() {
        val currentInteraction = _currentUserInteractionState.value
        val linkToUpdate = currentLink.value ?: return

        when (currentInteraction) {
            UserInteractionState.LIKED -> {
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentLink.update { current -> current?.copy(likesMobile = current.likesMobile - 1) }
                viewModelScope.launch {
                    apiService.incrementView(linkToUpdate.url, "unlikes")
                }
                Log.d(TAG, "Link unliked: ${linkToUpdate.name}")
            }

            UserInteractionState.DISLIKED -> {
                // Undislike
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentLink.update { current -> current?.copy(dislikesMobile = current.dislikesMobile - 1) }
                viewModelScope.launch {
                    apiService.incrementView(linkToUpdate.url, "undislikes")
                }
                Log.d(TAG, "Link undisliked: ${linkToUpdate.name}")

                // Like
                _currentUserInteractionState.value = UserInteractionState.LIKED
                _currentLink.update { current -> current?.copy(likesMobile = current.likesMobile + 1) }
                viewModelScope.launch {
                    apiService.incrementView(linkToUpdate.url, "likes")
                }
                Log.d(TAG, "Link liked: ${linkToUpdate.name}")
            }

            UserInteractionState.NONE -> {
                _currentUserInteractionState.value = UserInteractionState.LIKED
                _currentLink.update { current -> current?.copy(likesMobile = current.likesMobile + 1) }
                viewModelScope.launch {
                    apiService.incrementView(linkToUpdate.url, "likes")
                }
                Log.d(TAG, "Link liked: ${linkToUpdate.name}")
            }
        }
    }

    fun dislikeLink() {
        val currentInteraction = _currentUserInteractionState.value
        val linkToUpdate = currentLink.value ?: return

        when (currentInteraction) {
            UserInteractionState.DISLIKED -> {
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentLink.update { current -> current?.copy(dislikesMobile = current.dislikesMobile - 1) }
                viewModelScope.launch {
                    apiService.incrementView(linkToUpdate.url, "undislikes")
                }
                Log.d(TAG, "Link undisliked: ${linkToUpdate.name}")
            }

            UserInteractionState.LIKED -> {
                // Unlike
                _currentUserInteractionState.value = UserInteractionState.NONE
                _currentLink.update { current -> current?.copy(likesMobile = current.likesMobile - 1) }
                viewModelScope.launch {
                    apiService.incrementView(linkToUpdate.url, "unlikes")
                }
                Log.d(TAG, "Link unliked: ${linkToUpdate.name}")

                // DisLike
                _currentUserInteractionState.value = UserInteractionState.DISLIKED
                _currentLink.update { current -> current?.copy(dislikesMobile = current.dislikesMobile + 1) }
                viewModelScope.launch {
                    apiService.incrementView(linkToUpdate.url, "dislikes")
                }
                Log.d(TAG, "Link disliked: ${linkToUpdate.name}")
            }

            UserInteractionState.NONE -> {
                _currentUserInteractionState.value = UserInteractionState.DISLIKED
                _currentLink.update { current -> current?.copy(dislikesMobile = current.dislikesMobile + 1) }
                viewModelScope.launch {
                    apiService.incrementView(linkToUpdate.url, "dislikes")
                }
                Log.d(TAG, "Link disliked: ${linkToUpdate.name}")
            }
        }
    }


    fun openLink() {
        currentLink.value?.let { link ->
            _currentWebViewUrl.value = link.url
            _showWebView.value = true
        }
    }

    fun closeWebView() {
        _showWebView.value = false
        // FIX: Set the current link to the one we were just viewing.
        _currentLink.value = linkHistory.getOrNull(currentIndex)
        _currentWebViewUrl.value = null
    }

    fun showAddLinkDialog() {
        _showAddLinkDialog.value = true
    }

    fun hideAddLinkDialog() {
        _showAddLinkDialog.value = false
    }

    fun addLink(name: String, url: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            val request = Link(name = name, url = url, description = description, tags = tags)
            val result = apiService.addLink(request)
            val message = when (result) {
                is AddLinkResult.Success -> {
                    hideAddLinkDialog() // Hide dialog on success
                    "Link submitted for spam review successfully! The link will be live globally after review approval 🎉"
                }

                is AddLinkResult.Duplicate -> "This link already exists."
                is AddLinkResult.NetworkError -> "Network error. Please check your connection."
                is AddLinkResult.Error -> result.message
            }
            _toastMessage.value = message // Set the message for the UI to observe
        }
    }

    // Call this from the UI after the toast is shown
    fun toastMessageShown() {
        _toastMessage.value = null
    }
}
