package com.example.swapit1.model

enum class RequestState(val value: String) {
    PENDING("قيد الانتظار"),
    ACCEPTED("مقبول"),
    REJECTED("مرفوض");

    companion object {
        fun fromValue(value: String): RequestState {
            return values().firstOrNull { it.value == value } ?: PENDING
        }
    }
}
