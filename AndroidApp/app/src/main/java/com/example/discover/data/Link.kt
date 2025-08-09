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

data class LinkGson(
    val id: String,
    val name: String,
    val url: String,
    val description: String,
    val category: String,
    val views: Int,
    val likes: Int,
    val dislikes: Int
)