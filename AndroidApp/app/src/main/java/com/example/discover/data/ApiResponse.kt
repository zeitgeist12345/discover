package com.example.discover.data

data class AddWebsiteRequest(
    val name: String,
    val url: String,
    val description: String,
    val category: String = "user-submitted-mobile",
    val views: String = "0",
    val likes: String = "0",
    val dislikes: String = "0",
    val likesDesktop: String = "0",
    val dislikesDesktop: String = "0"
) 