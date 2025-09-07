package com.example.swapit1.ui.search

enum class SortOption { NEWEST, OLDEST }

data class FilterOptions(
    var sort: SortOption = SortOption.NEWEST,
    var location: String? = null,   // null = الكل
    var category: String? = null    // null = الكل
)
