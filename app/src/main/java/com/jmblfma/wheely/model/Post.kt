package com.jmblfma.wheely.model

import java.util.Date

data class Post(
    val postId:Int,
    val userName: String,
    val userSurname: String,
    val trackPlace: String,
    val trackDescription: String,
    val postDate: Date
    ){}
