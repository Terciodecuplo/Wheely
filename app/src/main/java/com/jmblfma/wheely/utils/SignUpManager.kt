package com.jmblfma.wheely.utils

object SignUpManager {
    var userProfilePictureCandidate : String? = null
    var vehiclePictureCandidate : String? = null

    fun restoreState(){
        userProfilePictureCandidate = null
        vehiclePictureCandidate = null
    }

    fun updateProfileImageCandidate(path: String) {
        this.userProfilePictureCandidate = path
    }

    fun updateVehiclePictureCandidate(path: String) {
        this.vehiclePictureCandidate = path
    }
}