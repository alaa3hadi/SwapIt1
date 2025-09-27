package com.example.swapit1.model

data class Notification(
    val iconRes: Int,
    val title: String,
    val message: String,
    val timeText: String,
    val seen: Boolean = false
)