package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.SessionType

class Converters {
    @TypeConverter
    fun fromSessionType(type: SessionType): String = type.name

    @TypeConverter
    fun toSessionType(value: String): SessionType = SessionType.fromString(value)
}
