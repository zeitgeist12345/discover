/* Copyright (c) 2025 Mohammad Sheraj *//* Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

package com.example.discover.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.edit
import androidx.core.net.toUri
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

private const val TAG = "DiscoverViewModel"

// Enum to represent user interaction
enum class UserInteractionState {
    NONE, LIKED, DISLIKED
}

class DiscoverViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val apiService = ApiService()
    private val prefs = application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

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
    private val _timeStats = MutableStateFlow(TimeStats(0, 0, 0, 0, 0, 0, 0))
    val timeStats: StateFlow<TimeStats> = _timeStats.asStateFlow()

    // Persistent Settings Filter State - Default to "positive, daily" for tagsAllowlist
    val tagsAllowlist =
        MutableStateFlow(prefs.getString("tags_allow", "positive, daily") ?: "positive, daily")
    val tagsBlocklist = MutableStateFlow(prefs.getString("tags_block", "optional") ?: "optional")
    val urlsAllowlist = MutableStateFlow(prefs.getString("urls_allow", "") ?: "")
    val urlsBlocklist = MutableStateFlow(prefs.getString("urls_block", "") ?: "")

    // Persistent read‑only mode (default ON)
    val readOnlyModeEnabled = MutableStateFlow(prefs.getBoolean("read_only_mode", true))

    init {
        loadTimeStats()
    }

    fun setReadOnlyMode(enabled: Boolean) {
        readOnlyModeEnabled.value = enabled
        prefs.edit { putBoolean("read_only_mode", enabled) }
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

    fun applyFilters() {
        prefs.edit {
            putString("tags_allow", tagsAllowlist.value)
            putString("tags_block", tagsBlocklist.value)
            putString("urls_allow", urlsAllowlist.value)
            putString("urls_block", urlsBlocklist.value)
        }
        updateLinksInBackground(0)
    }

    fun resetToDefaults() {
        tagsAllowlist.value = "positive, daily"
        tagsBlocklist.value = "optional"
        urlsAllowlist.value = ""
        urlsBlocklist.value = ""
        _toastMessage.value = "Settings reset: Filters cleared"
        applyFilters()
    }

    private fun updateLinksInBackground(logUser: Int) {
        viewModelScope.launch {
            try {
                val tagsAllow =
                    tagsAllowlist.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val tagsBlock =
                    tagsBlocklist.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val urlsAllow =
                    urlsAllowlist.value.split(" ").map { it.trim() }.filter { it.isNotEmpty() }
                val urlsBlock =
                    urlsBlocklist.value.split(" ").map { it.trim() }.filter { it.isNotEmpty() }

                val linksList =
                    apiService.getLinks(tagsAllow, tagsBlock, urlsAllow, urlsBlock, logUser)
                if (linksList.isNotEmpty()) {
                    _isApiAvailable.value = 1
                    _links.value = linksList

                    if (tagsAllow.isNotEmpty() || tagsBlock.isNotEmpty() || urlsAllow.isNotEmpty() || urlsBlock.isNotEmpty()) {
                        _toastMessage.value = "✅ Filter applied: ${linksList.size} links found"
                    } else {
                        _toastMessage.value = "✅ Filters cleared: ${linksList.size} links found"
                    }
                } else {
                    _isApiAvailable.value = -1
                    _toastMessage.value = "No links found for the applied filters"
                    _links.value = emptyList()
                }
            } catch (e: Exception) {
                _isApiAvailable.value = -1
                _toastMessage.value = "❌ Failed to update links"
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
        updateLinksInBackground(1)
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
            val message = when (val result = apiService.addLink(request)) {
                is AddLinkResult.Success -> {
                    hideAddLinkDialog() // Hide dialog on success
                    "Link submitted for spam review successfully! The link will be live globally after review and approval 🎉"
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

    fun copyToClipboard(text: String) {
        val clipboard =
            getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Discover Link", text)
        clipboard.setPrimaryClip(clip)
        _toastMessage.value = "Link copied to clipboard"
    }

    fun downloadMedia(url: String, userAgent: String) {
        try {
            val request = DownloadManager.Request(url.toUri())
            request.addRequestHeader("User-Agent", userAgent)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            // Extract file name from URL or use a default
            var fileName =
                url.substringAfterLast("/").substringBefore("?").ifEmpty { "downloaded_file" }

            // Ensure file has an extension
            if (!fileName.contains(".")) {
                val ext = MimeTypeMap.getFileExtensionFromUrl(url)
                if (ext.isNotEmpty()) {
                    fileName = "$fileName.$ext"
                }
            }

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager =
                getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            _toastMessage.value = "Download started..."
        } catch (e: Exception) {
            _toastMessage.value = "Download failed: ${e.message}"
            e.printStackTrace()
        }
    }
}
