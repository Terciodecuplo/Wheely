package com.jmblfma.wheely.data
import android.content.Context
import androidx.room.TypeConverter
import com.jmblfma.wheely.R

enum class Difficulty(val value: Int) {
    UNKNOWN(0),
    EASY(1),
    MEDIUM(2),
    HARD(3);

    fun getLocalizedName(context: Context): String {
        return when (this) {
            UNKNOWN -> context.getString(R.string.difficulty_none)
            EASY -> context.getString(R.string.difficulty_easy)
            MEDIUM -> context.getString(R.string.difficulty_medium)
            HARD -> context.getString(R.string.difficulty_hard)
        }
    }
    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

class DifficultyConverters {
    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty): Int = difficulty.value

    @TypeConverter
    fun toDifficulty(value: Int): Difficulty = Difficulty.fromInt(value)
}

