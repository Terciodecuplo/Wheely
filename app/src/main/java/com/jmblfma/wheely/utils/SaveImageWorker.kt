package com.jmblfma.wheely.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SaveImageWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val uri = inputData.getString("uri")?.let { Uri.parse(it) }
            val fileName = inputData.getString("fileName")
            val entityType = inputData.getString("entityType")
            val imageType = inputData.getString("imageType")
            val entityId = inputData.getInt("entityId", -1)

            val bitmap = uri?.let { ImagePicker.fixImageOrientation(applicationContext, it) }
            if (bitmap == null) {
                Log.e("SaveImageWorker", "Failed to fix image orientation or decode bitmap")
                return@withContext Result.failure()
            }

            val savedPath =
                fileName?.let {
                    ImagePicker.saveImageToInternalStorage(
                        applicationContext, bitmap,
                        it
                    )
                }
            if (savedPath == null) {
                Log.e("SaveImageWorker", "Failed to save image to internal storage")
                return@withContext Result.failure()
            }

            if (entityType != null && imageType != null) {
                updateImagesInDataBase(entityId, entityType, imageType, savedPath)
            }
            if (entityId != -1) sendUpdateBroadcast()
            Result.success()
        } catch (e: Exception) {
            Log.e("SaveImageWorker", "Error processing image", e)
            Result.failure()
        } finally {

        }
    }


    private fun updateImagesInDataBase(
        entityId: Int,
        entityType: String,
        imageType: String,
        path: String
    ) {
        Log.d("SaveImageWorker", "entityId ======> $entityId path ========> $path")
        runBlocking {
            when (entityType) {
                "user" -> {
                    val userRepository = UserDataRepository.sharedInstance
                    if (imageType == "banner") {
                        userRepository.updateUserBanner(entityId, path) { result ->
                            updateCurrentUserImageData(null, path)
                            Log.d("SaveImageWorker", "User banner updated, affected rows: $result")

                        }
                    } else if (imageType == "profile") {
                        if (entityId != -1) {

                            userRepository.updateUserPersonalInfo(
                                entityId,
                                null,
                                null,
                                null,
                                null,
                                path
                            ) { result ->
                                Log.d(
                                    "SaveImageWorker",
                                    "User profile image updated, affected rows: $result"
                                )
                                updateCurrentUserImageData(path, null)
                            }
                        } else {
                            SignUpManager.updateProfileImageCandidate(path)
                        }
                    }
                }

                "vehicle" -> {
                    if (imageType == "vehicle") {
                        SignUpManager.updateVehiclePictureCandidate(path)
                    }
                }
            }
        }
    }

    private fun sendUpdateBroadcast() {
        val intent = Intent("com.jmblfma.wheely.UPDATE_USER_INFO")
        applicationContext.sendBroadcast(intent)
    }

    private fun updateCurrentUserImageData(
        profileImagePathCandidate: String?,
        bannerPathCandidate: String?
    ) {
        val updatedUser = UserSessionManager.getCurrentUser()?.let {
            User(
                it.userId,
                it.nickname,
                it.firstName,
                it.lastName,
                it.email,
                it.dateOfBirth,
                profileImagePathCandidate ?: it.profileImage,
                bannerPathCandidate ?: it.profileBanner
            )
        }
        UserSessionManager.updateLoggedUser(updatedUser)
    }

}
