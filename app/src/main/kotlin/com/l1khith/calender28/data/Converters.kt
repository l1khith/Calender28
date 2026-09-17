package com.l1khith.calender28.data

import androidx.room.TypeConverter
import org.json.JSONArray

/**
 * Room TypeConverter for converting primitive collections like [List]<[Int]>
 * to and from standard JSON string representations.
 */
class Converters {

    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrBlank() || value == "[]") return emptyList()
        return try {
            val array = JSONArray(value)
            val list = mutableListOf<Int>()
            for (i in 0 until array.length()) {
                list.add(array.getInt(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
