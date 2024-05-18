package com.jmblfma.wheely.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan

object StyleUtils {
    fun getStyledMagnitude(value: String, unitScale: Float = 0.5f): SpannableString {
        val spannableString = SpannableString(value)

        // finds the start of the unit (e.g 0.00 Km/h OR 0 Km/h -first is.Letter=true)
        val numberEndIndex = value.indexOfFirst { it.isLetter() }

        // validates input format; returns same string if it doesn't apply
        // only checks for controlled cases within the app
        // TODO improve validation logic for ALL cases for safety?
        if (value.isEmpty() || value == TrackAnalysis.FAILED_CALC_MSG) {
            return SpannableString(value)
        }

        // applies the different size factor to the units
        if (numberEndIndex != -1) {
            spannableString.setSpan(
                RelativeSizeSpan(unitScale), // scale the unit relative to the number size
                numberEndIndex, value.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannableString
    }

    fun getStyledDuration(time: String, hideZeroHours: Boolean = true, hideZeroMinutes: Boolean = false): SpannableString {
        val hourNumberSize = 1f
        val hourUnitSize = 0.5f

        val minuteNumberSize = 1f
        val minuteUnitSize = 0.5f

        val secondNumberSize = 0.7f
        val secondUnitSize = 0.3f

        // validates input format; returns same string if it doesn't apply
        // only checks for controlled cases within the app
        // TODO improve validation logic for ALL cases for safety?
        if (time.isEmpty() || time == TrackAnalysis.FAILED_CALC_MSG) {
            return SpannableString(time)
        }

        // splits the time string into parts
        val parts = time.split(" ")
        val hours = parts[0] + " " + parts[1]  // "0 h"
        val minutes = parts[2] + " " + parts[3]  // "0 min"
        val seconds = parts[4] + " " + parts[5]  // "0 s"

        // sets fields show/hide
        val displayHours = !(hideZeroHours && parts[0] == "0")
        val displayMinutes = !(hideZeroMinutes && parts[2] == "0")

        // builds the final string
        val displayTime = StringBuilder()
        if (displayHours) displayTime.append(hours).append(" ")
        if (displayMinutes) displayTime.append(minutes).append(" ")
        displayTime.append(seconds)

        // creates a SpannableString from the final string
        val spannableString = SpannableString(displayTime.toString().trim())

        // applies size spans to different parts of the text
        var start = 0
        if (displayHours) {
            val end = start + hours.length
            spannableString.setSpan(
                RelativeSizeSpan(hourNumberSize), // Bigger size for hour numbers
                start, end - 2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                RelativeSizeSpan(hourUnitSize), // Smaller size for hour unit
                end - 2, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = end + 1
        }

        if (displayMinutes) {
            val end = start + minutes.length
            spannableString.setSpan(
                RelativeSizeSpan(minuteNumberSize), // Bigger size for minute numbers
                start, end - 4,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                RelativeSizeSpan(minuteUnitSize), // Smaller size for minute unit
                end - 4, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = end + 1
        }

        val secondsStart = start
        val secondsEnd = secondsStart + seconds.length
        spannableString.setSpan(
            RelativeSizeSpan(secondNumberSize), // Bigger size for second numbers
            secondsStart, secondsEnd - 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            RelativeSizeSpan(secondUnitSize), // Smaller size for second unit
            secondsEnd - 2, secondsEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }
}