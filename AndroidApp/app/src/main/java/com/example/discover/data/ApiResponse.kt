package com.example.discover.data

data class AddWebsiteRequest(
    val name: String,
    val url: String,
    val description: String,
    val category: String = "user-submitted-mobile"
) 