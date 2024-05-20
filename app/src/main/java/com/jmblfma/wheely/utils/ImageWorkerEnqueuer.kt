package com.jmblfma.wheely.utils

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.UUID

object ImageWorkerUtil {
    fun enqueueImageSave(
        context: Context,
        uri: Uri,
        entityId: Int?,
        entityType: String,
        imageType: String,
        prefix: String
    ) {
        val imageId = UUID.randomUUID().toString()
        val fileName = "$prefix-$imageId.jpg"

        val inputData = workDataOf(
            "uri" to uri.toString(),
            "fileName" to fileName,
            "entityType" to entityType,
            "imageType" to imageType,
            "entityId" to entityId,
            "outputPath" to context.filesDir.absolutePath
        )

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val saveImageWorkRequest = OneTimeWorkRequestBuilder<SaveImageWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(saveImageWorkRequest)
        // Log.d("SaveImageWorker", " INPUT DATA =====> $inputData")

    }
}
