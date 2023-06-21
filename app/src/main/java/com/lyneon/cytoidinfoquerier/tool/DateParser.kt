package com.lyneon.cytoidinfoquerier.tool

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateParser {
    fun parseISO8601Date(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return dateFormat.parse(dateString) as Date
    }

    fun Date.formatToGMT8String(): String {
        val time = this.time + 28800000
        val date = Date(time)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
        return dateFormat.format(date)
    }
}