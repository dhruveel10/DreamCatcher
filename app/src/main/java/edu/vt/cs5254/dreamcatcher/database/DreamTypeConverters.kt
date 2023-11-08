package edu.vt.cs5254.dreamcatcher.database

import androidx.room.TypeConverter
import java.util.Date

class DreamTypeConverters {
    @TypeConverter
    fun getDateFromLong(millis: Long): Date{
        return Date(millis)
    }

    @TypeConverter
    fun getLongFromDate(date: Date): Long{
        return date.time
    }
}