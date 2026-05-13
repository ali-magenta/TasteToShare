package com.example.tastetoshare.data

enum class Cost(val displayName: String) {
    CHEAP("€"),
    AFFORDABLE("€€"),
    EXPENSIVE("€€€");

    companion object {
        fun fromDisplayName(name: String): Cost? {
            return Cost.entries.find { it.displayName == name }
        }
    }
}