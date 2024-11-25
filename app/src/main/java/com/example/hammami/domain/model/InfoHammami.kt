package com.example.hammami.domain.model

data class InfoHammami(
    val title: String,
    val icon: Int,
    val desc: String,
    val image: Int?,
    var isExpandable: Boolean = false
)