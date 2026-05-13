package com.example.tastetoshare.data

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    companion object {
        fun fromDisplayName(name: String): Difficulty? {
            return entries.find { it.displayName == name }
        }
    }
}