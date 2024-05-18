package com.jmblfma.wheely.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getString
import com.jmblfma.wheely.R

object DialogUtils {
    fun showConfirmationDialog(
        context: Context,
        message: String,
        positiveButtonText: String = getString(context, R.string.confirmation_dialog_positive),
        negativeButtonText: String = getString(context, R.string.confirmation_dialog_negative),
        onPositiveAction: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)

        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveAction()
            dialog.dismiss()
        }

        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
