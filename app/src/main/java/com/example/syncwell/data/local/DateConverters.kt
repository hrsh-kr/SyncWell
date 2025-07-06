package com.example.syncwell.data.local

import org.threeten.bp.LocalDate

/**
 * Room type converter for LocalDate
 */
class DateConverters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}