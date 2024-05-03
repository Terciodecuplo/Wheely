package com.jmblfma.wheely.model


data class User(
    val userId: Int = 0,
    val userName:String = "DefaultUser",
    val firstName:String = "-",
    val lastName: String = "-",
    val email:String = "-",
    val dateOfBirth:String = "-",
    val drivenTracks: ArrayList<TrackTest>? = null,
    val ownedVehicles: ArrayList<Vehicle>? = null
){

}
