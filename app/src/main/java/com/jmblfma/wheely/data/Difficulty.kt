package com.jmblfma.wheely.data
import androidx.core.content.ContextCompat.getString
import androidx.room.TypeConverter
import com.jmblfma.wheely.MyApp
import com.jmblfma.wheely.R

enum class Difficulty(val value: Int) {
    UNKNOWN(0){
        override fun toString() = getString(MyApp.applicationContext(), R.string.difficulty_none)
    },
    EASY(1){
        override fun toString() = getString(MyApp.applicationContext(), R.string.difficulty_easy)
    },
    MEDIUM(2){
        override fun toString() = getString(MyApp.applicationContext(), R.string.difficulty_medium)
    },
    HARD(3){
        override fun toString() = getString(MyApp.applicationContext(), R.string.difficulty_hard)
    };

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

