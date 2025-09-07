package com.example.swapit1.model

import com.google.firebase.firestore.DocumentId


data class Offers(
    @DocumentId
    var   offerId : String = "",
      val ownerId: String = "",
      val ownerName: String = "",
      val productName: String = "",
      val requestedProduct: String = "",
      val category: String = "",
      val location: String = "",
      val description: String = "",
      val images: List<String> = emptyList(),
      val createdAt: com.google.firebase.Timestamp? = null
)
