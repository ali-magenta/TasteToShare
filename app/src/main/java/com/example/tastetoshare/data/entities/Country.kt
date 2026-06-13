package com.example.tastetoshare.data.entities

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Country @OptIn(ExperimentalUuidApi::class) constructor(
    val countryId: String = Uuid.random().toString(),
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val size: Double = 0.0 // The size in km of the average radius of the country on a mercator projection (so close to the equator it's the real radius but greenland should be bigger)
)