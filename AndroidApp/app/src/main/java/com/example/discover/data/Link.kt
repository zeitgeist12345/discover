package com.example.discover.data

import com.google.gson.annotations.SerializedName

data class Link(
    val name: String,
    val url: String,
    val description: String,
    val category: String = "",
    val views: Int = 0,
    val likes: Int = 0,
    val dislikes: Int = 0,
    @SerializedName("createdAt")
    val createdAt: String = "",
    val id: String = "",
    val active: Boolean = true
) 