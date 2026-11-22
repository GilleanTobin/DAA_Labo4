package ch.heigvd.iict.daa.template.models

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.util.*

@Entity(indices = [Index(value = ["ownerId"], unique = true)])
data class Schedule(
    @PrimaryKey(autoGenerate = true) var scheduleId : Long?,
    var ownerId : Long,
    var date : Calendar
)
