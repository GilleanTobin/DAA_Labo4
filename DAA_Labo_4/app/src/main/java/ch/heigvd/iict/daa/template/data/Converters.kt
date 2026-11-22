package ch.heigvd.iict.daa.template.data

import androidx.room3.ColumnTypeConverter
import java.util.Calendar

class Converters {
    @ColumnTypeConverter
    fun calendarToLong(calendar: Calendar?): Long? = calendar?.timeInMillis

    @ColumnTypeConverter
    fun longToCalendar(value: Long?): Calendar? =
        value?.let { Calendar.getInstance().apply { timeInMillis = it } }
}
