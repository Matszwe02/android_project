package com.example.listapp

data class ShoppingList(
    val id: String = "",
    val users: List<String> = emptyList(),
    val title: String = "",
    val icon: Int = 0,
    val content: String = ""
)
