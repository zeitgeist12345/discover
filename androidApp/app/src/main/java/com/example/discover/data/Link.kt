package com.example.discover.data

data class Link(
    val name: String,
    val url: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val views: Int = 0,
    val likesMobile: Int = 0,
    val dislikesMobile: Int = 0
)