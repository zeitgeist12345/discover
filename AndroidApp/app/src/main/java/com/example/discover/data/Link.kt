package com.example.discover.data

data class Link(
    val id: String = "",
    val name: String,
    val url: String,
    val description: String,
    val category: String = "user-submitted-mobile",
    val views: Int = 0,
    val likes: Int = 0,
    val dislikes: Int = 0
)