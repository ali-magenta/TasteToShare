package com.example.tastetoshare.data

import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.Review
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.entities.User

data class WrapperPaperDB (
    val users: List<User>,
    val recipes: List<Recipe>,
    val tags: List<Tag>,
    val ingredients: List<Ingredient>,
    val reviews: List<Review>
)