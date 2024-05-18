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
            Log.d("SaveImageWorker", "fileName ====> ${inputData.getString("prefix")}")
            val uri = inputData.getString("uri")?.let { Uri.parse(it) }
            val fileName = inputData.getString("fileName")
            val entityType = inputData.getString("entityType")
            val imageType = inputData.getString("imageType")
            val entityId = inputData.getInt("entityId", -1)

            if (uri == null || fileName == null || entityType == null || imageType == null || entityId == -1) {
                Log.e(
                    "SaveImageWorker",
                    "Invalid input data: uri=$uri, fileName=$fileName, entityType=$entityType, imageType=$imageType, entityId=$entityId"
                )
                return@withContext Result.failure()
            }

            val bitmap = ImagePicker.fixImageOrientation(applicationContext, uri)
            if (bitmap == null) {
                Log.e("SaveImageWorker", "Failed to fix image orientation or decode bitmap")
                return@withContext Result.failure()
            }

            val savedPath =
                ImagePicker.saveImageToInternalStorage(applicationContext, bitmap, fileName)
            if (savedPath == null) {
                Log.e("SaveImageWorker", "Failed to save image to internal storage")
                return@withContext Result.failure()
            }

            updateImagesInDataBase(entityId, entityType, imageType, savedPath)
            Result.success()
        } catch (e: Exception) {
            Log.e("SaveImageWorker", "Error processing image", e)
            Result.failure()
        }
    }


    private fun updateImagesInDataBase(
        entityId: Int,
        entityType: String,
        imageType: String,
        path: String
    ) {
        // Use the repository for user updates
        Log.d("SaveImageWorker", "entityId ======> $entityId path ========> $path")
        runBlocking {
            when (entityType) {
                "user" -> {
                    val userRepository = UserDataRepository.sharedInstance
                    if (imageType == "banner") {
                        userRepository.updateUserBanner(entityId, path) { result ->
                            updateCurrentUserBanner(path)
                            Log.d("SaveImageWorker", "User banner updated, affected rows: $result")

                        }
                    } else if (imageType == "profile") {
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
                        }
                    }
                }
                /*  "vehicle" -> {
                      if (imageType == "vehicle") {
                          // Assume you have a VehicleDataRepository or similar
                          val vehicleRepo = VehicleDataRepository.sharedInstance
                          vehicleRepo.updateVehicleImage(entityId, path) { result ->
                              Log.d("SaveImageWorker", "Vehicle image updated, affected rows: $result")
                          }
                      }
                  }*/
            }
        }
    }

    private fun sendUpdateBroadcast() {
        val intent = Intent("com.jmblfma.wheely.UPDATE_USER_INFO")
        applicationContext.sendBroadcast(intent)
    }

    private fun updateCurrentUserBanner(bannerPath: String) {
        val updatedUser = UserSessionManager.getCurrentUser()?.let {
            User(
                it.userId,
                it.nickname,
                it.firstName,
                it.lastName,
                it.email,
                it.dateOfBirth,
                it.profileImage,
                bannerPath
            )
        }
        UserSessionManager.updateLoggedUser(updatedUser)
        sendUpdateBroadcast()
        Log.d(
            "SaveImageWorker",
            "User profile image updated: $bannerPath ||||  ${UserSessionManager.getCurrentUser()?.profileBanner}"
        )

    }
}
