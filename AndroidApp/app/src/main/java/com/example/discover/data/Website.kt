package com.example.discover.data

import com.google.gson.annotations.SerializedName

data class Website(
    val id: String,
    val name: String,
    val url: String,
    val description: String,
    val category: String? = null,
    val views: Int = 0,
    val likes: Int = 0,
    val dislikes: Int = 0,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    val active: Boolean = true
) 