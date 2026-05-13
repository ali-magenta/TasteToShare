package com.example.tastetoshare.data

import com.example.tastetoshare.R

enum class Roles(val displayName: String, val description: String, val icon: Int) {
    PROFESSIONAL_CHEF(
        "Professional Chef",
        "Culinary experts who cook for a living",
        R.drawable.pan_pot
    ),
    HOME_COOK(
        "Home Cook",
        "Everyday cooks who love sharing their recipes",
        R.drawable.food_bank_home_cook
    ),
    FOOD_ENTHUSIAST(
        "Food Enthusiast",
        "People who love to eat and discover new tastes",
        R.drawable.restaurant_enthusiast
    );

    companion object {
        fun fromDisplayName(name: String): Roles? =
            entries.find { it.displayName == name }
    }
}