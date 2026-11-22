/*
 * DAA - Laboratoire 4
 * Code fourni, modifié par : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 * Modification : index unique sur ownerId pour garantir la relation One-to-One (0..1)
 * entre Note et Schedule au niveau de la base de données.
 */
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
