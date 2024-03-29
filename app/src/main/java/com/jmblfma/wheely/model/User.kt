package com.jmblfma.wheely.model


data class User(
    val userId: Int,
    val name:String,
    val firstName:String,
    val lastName: String,
    val email:String,
    val dateOfBirth:String,
    val drivenTracks: ArrayList<Track>,
    val ownedVehicles: ArrayList<Vehicle>
){

}
