/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.data

import androidx.room3.ColumnTypeConverter
import java.util.Calendar

/** Conversion des dates (Calendar) en timestamp (ms) pour le stockage SQLite. */
class Converters {
    @ColumnTypeConverter
    fun calendarToLong(calendar: Calendar?): Long? = calendar?.timeInMillis

    @ColumnTypeConverter
    fun longToCalendar(value: Long?): Calendar? =
        value?.let { Calendar.getInstance().apply { timeInMillis = it } }
}
