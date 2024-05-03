package com.jmblfma.wheely.model

import java.time.ZonedDateTime

data class Post(
    val postId: Int,
    val description: String,
    val postedBy: User,
    val associatedTrack: TrackTest,
    val datePublished: ZonedDateTime
) {}
