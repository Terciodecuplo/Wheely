package com.jmblfma.wheely.model

import java.time.LocalDate

data class Post(
    val postId: Int,
    val userName: String,
    val userSurname: String,
    val trackPlace: String,
    val trackDescription: String,
    val postDate: String
) {}
