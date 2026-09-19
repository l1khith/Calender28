package com.l1khith.calender28.data.user

object DisplayNameGenerator {

    /**
     * Generate a display name from a creation timestamp.
     *
     * Format: "User" + random4digits + last6digitsOfTimestamp
     * Example: User4721987654
     *
     * @param creationTimestampMs - Unix timestamp in milliseconds
     *                              captured ONCE at account creation
     */
    fun generate(creationTimestampMs: Long): String {
        val random4 = (1000..9999).random()
        val last6 = (creationTimestampMs % 1_000_000L)
            .toString()
            .padStart(6, '0')
        return "User$random4$last6"
    }
}
