package com.jmblfma.wheely.data
import androidx.room.TypeConverter

enum class Difficulty(val value: Int) {
    UNKNOWN(0){
        override fun toString() = "Unknown"
    },
    EASY(1){
        override fun toString() = "Easy"
    },
    MEDIUM(2){
        override fun toString() = "Medium"
    },
    HARD(3){
        override fun toString() = "Hard"
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

