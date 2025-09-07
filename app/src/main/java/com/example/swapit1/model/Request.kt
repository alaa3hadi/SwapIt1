package com.example.swapit1.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Request(
    @DocumentId
    var requestId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val productId: String = "",
    val productName: String = "",
    val correspondingProduct: String = "",
    val category: String = "",
    val location: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val state: RequestState = RequestState.PENDING // أو أي default يناسبك
)
