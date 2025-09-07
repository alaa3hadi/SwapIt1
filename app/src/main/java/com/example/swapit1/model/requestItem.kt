package com.example.swapit1.model

data class requestItem (
    val wantProduct: String,
    val haveProduct: String,
    val imageResId: Int ,
    val state:  RequestState
)