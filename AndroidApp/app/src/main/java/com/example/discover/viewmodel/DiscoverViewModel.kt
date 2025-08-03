package com.example.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discover.data.AddWebsiteRequest
import com.example.discover.data.StaticWebsites
import com.example.discover.data.Website
import com.example.discover.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel : ViewModel() {
    private val apiService = ApiService()
    
    private val _websites = MutableStateFlow<List<Website>>(emptyList())
    val websites: StateFlow<List<Website>> = _websites.asStateFlow()
    
    private val _currentWebsite = MutableStateFlow<Website?>(null)
    val currentWebsite: StateFlow<Website?> = _currentWebsite.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
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
        loadWebsites()
    }
    
    fun loadWebsites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val websitesList = apiService.getWebsites()
                
                if (websitesList.isNotEmpty()) {
                    _websites.value = websitesList
                    // Clear any existing state
                    _currentWebsite.value = null
                    visitedWebsites.clear()
                    websiteHistory.clear()
                    currentIndex = -1
                    
                    // Load the first random website
                    loadRandomWebsite()
                } else {
                    // Fallback to static websites
                    _websites.value = StaticWebsites.websites
                    _error.value = "Using offline websites. API unavailable."
                    
                    // Load the first random website from static data
                    loadRandomWebsite()
                }
            } catch (e: Exception) {
                // Fallback to static websites on any error
                _websites.value = StaticWebsites.websites
                _error.value = "Using offline websites. Network error: ${e.message}"
                
                // Load the first random website from static data
                loadRandomWebsite()
            } finally {
                _isLoading.value = false
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