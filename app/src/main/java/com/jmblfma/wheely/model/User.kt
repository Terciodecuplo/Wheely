package com.jmblfma.wheely.model

data class User(
    val userId: Int,
    val userName:String,
    val userSurname:String,
    val userEmail:String,
    val userBirthday:String,
    val trackIds:MutableList<Track>
){

}
