package com.kuss.krude.utils

import android.text.format.DateFormat
import android.text.format.DateUtils
import java.util.Date

object DayHelper {
    fun fromNow(date: Date): CharSequence? {
        val now = System.currentTimeMillis()
        val diff: Long = now - date.time
        val format = DateUtils.getRelativeTimeSpanString(
            date.time,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL
        )

        return if (diff < DateUtils.HOUR_IN_MILLIS) {
            format
        } else if (diff < 2 * DateUtils.DAY_IN_MILLIS) {
            "$format ${DateFormat.format("HH:mm", date.time)}"
        } else {
            DateFormat.format("yyyy-MM-dd HH:mm", date.time)
        }
    }
}