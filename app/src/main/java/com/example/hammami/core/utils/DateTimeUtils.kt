package com.example.hammami.core.utils

import java.time.LocalDate
import java.time.ZoneId


object DateTimeUtils { // Utilizziamo un object per creare un singleton

    fun LocalDate.toMillis(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}