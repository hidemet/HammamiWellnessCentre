package com.example.hammami.core.time

import android.util.Log
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.time.LocalTime


object DateTimeUtils {

    fun createStartAndEndTimestamps(
        date: LocalDate,
        startTime: String,
        endTime: String
    ): Pair<Timestamp, Timestamp> {
        val startDateTime = LocalDateTime.of(date, LocalTime.parse(startTime))
        val endDateTime = LocalDateTime.of(date, LocalTime.parse(endTime))
        val startDate =
            Timestamp(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        val endDate = Timestamp(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        return Pair(startDate, endDate)
    }

    fun createStartAndEndTimestamps(
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Pair<Timestamp, Timestamp> {
        val startDateTime = LocalDateTime.of(date, startTime)
        val endDateTime = LocalDateTime.of(date, endTime)
        val startDate =
            Timestamp(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        val endDate = Timestamp(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        return Pair(startDate, endDate)
    }


    fun LocalDate.toStartOfDayTimestamp(): Timestamp {
        val startOfDay = this.atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Timestamp(Date.from(startOfDay))
    }

    fun LocalDate.toEndOfDayTimestamp(): Timestamp {
        val endOfDay =
            this.atStartOfDay().plusDays(1).minusSeconds(1).atZone(ZoneId.systemDefault())
                .toInstant()
        return Timestamp(Date.from(endOfDay))
    }

    fun LocalDateTime.toTimestamp(): Timestamp {
        return Timestamp(Date.from(this.atZone(ZoneId.systemDefault()).toInstant()))
    }

    fun formatDate(timestamp: Timestamp): String {
        return timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
    }

    fun formatTimeRange(startDate: Timestamp, endDate: Timestamp): String {
        val start = SimpleDateFormat("HH:mm", Locale.getDefault()).format(startDate.toDate())
        val end = SimpleDateFormat("HH:mm", Locale.getDefault()).format(endDate.toDate())
        return "$start - $end"
    }

    fun formatDateRange(startDate: Date, endDate: Date): String { //Modificato
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return "${dateFormatter.format(startDate)} - ${dateFormatter.format(endDate)}"
    }

    fun toTimestamp(date: LocalDate, time: LocalTime): Timestamp {
        val dateTime = LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(Date.from(dateTime))
    }

    fun toTimestamp(date: LocalDate, time: TimeSlot): Timestamp {
        val dateTime =
            LocalDateTime.of(date, time.startTime).atZone(ZoneId.systemDefault()).toInstant()
        val timestamp =  Timestamp(Date.from(dateTime))
        return timestamp
    }
    fun Timestamp.toLocalDate(): LocalDate {
        return this.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun Timestamp.toLocalTime(): LocalTime {
        return this.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
    }

    fun calculateServiceDurationInMinutes(startTime: LocalTime, endTime: LocalTime): Int {
        val duration = Duration.between(startTime, endTime)
        return duration.toMinutes().toInt()
    }
    fun formatTime(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    }

    fun localDateFromTimestamp(timestamp: Timestamp): LocalDate {
        return timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }


}