package com.example.discover.data

data class Link(
    val id: String,
    val name: String,
    val url: String,
    val description: String,
    val tags: List<String>,
    val views: Int,
    val likesMobile: Int,
    val dislikesMobile: Int
)