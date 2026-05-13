package com.example.tastetoshare.data.entities

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Step @OptIn(ExperimentalUuidApi::class) constructor(
    val stepId: String = Uuid.random().toString(),
    val photoURL: String? = null,
    val description: String = "",
    val order: Int = 0
)
