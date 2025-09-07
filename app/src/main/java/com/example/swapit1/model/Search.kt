package com.example.swapit1.model

import com.google.firebase.Timestamp

data class Search    (
    var id: String? = null,

    // اسم المنتج (بنبحث عليه)
    var productName: String = "",

    // وصف مختصر للعنصر
    var description: String = "",
    var requestedProduct: String = "",
    // فلاتر
    var location: String? = null,   // مثال: "غزة - الرمال" أو "غزة"
    var category: String? = null,   // مثال: "طعام"

    // وقت الإضافة
    var createdAt: Timestamp? = null,

    // مصفوفة صور Base64
    var images: List<String>? = emptyList()
)

data class FilterOptions(
    var location: String? = null,
    var category: String? = null,
    var sort: SortOption = SortOption.NEWEST
)

enum class SortOption { NEWEST, OLDEST }
