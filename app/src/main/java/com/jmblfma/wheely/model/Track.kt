package com.jmblfma.wheely.model

import java.util.Date

data class Track(
    val trackId: Int,
    val trackName:String,
    val trackPlace: String,
    //map??
    val trackPartners: User,
    val trackDifficulty: String,
    val trackRating: Long,
    val trackDate: Date
    ){}
