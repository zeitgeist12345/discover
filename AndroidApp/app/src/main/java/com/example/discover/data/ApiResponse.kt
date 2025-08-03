package com.example.discover.data

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

data class WebsiteStats(
    val views: Int,
    val likes: Int,
    val dislikes: Int
)

data class AddWebsiteRequest(
    val name: String,
    val url: String,
    val description: String,
    val category: String = "user-submitted-mobile"
) 