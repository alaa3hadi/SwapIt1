package com.example.swapit1.model

import android.net.Uri
import com.google.firebase.Timestamp

data class CardItem(
    val   offerId : String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val customerId : String = "" ,
    val productName: String = "",
    val requestedProduct: String = "",
    val category: String = "",
    val location: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val createdAt: com.google.firebase.Timestamp? = null
){
//    constructor(): this(0 , "" , "" , "" , "" )
}

