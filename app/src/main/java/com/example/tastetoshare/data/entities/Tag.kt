package com.example.tastetoshare.data.entities

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Tag @OptIn(ExperimentalUuidApi::class) constructor(
    val tagId: String = Uuid.random().toString(),
    val type: String = "",
    val name: String = ""
)
