package com.l1khith.calender28.utils

import java.util.TimeZone

fun currentTimeMillis(): Long = System.currentTimeMillis()

fun getTimeZoneOffset(timestampMs: Long): Long {
    return TimeZone.getDefault().getOffset(timestampMs).toLong()
}

