/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.data

import androidx.lifecycle.LiveData
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import ch.heigvd.iict.daa.template.models.Note
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import ch.heigvd.iict.daa.template.models.Schedule

/**
 * Accès aux tables Note et Schedule.
 * Les lectures sont observables (LiveData), les écritures sont des fonctions suspendues.
 */
@Dao
abstract class NoteDao {
    // @Transaction : la relation NoteAndSchedule nécessite deux requêtes qui doivent être cohérentes
    @Transaction
    @Query("SELECT * FROM Note")
    abstract fun getAllNotes(): LiveData<List<NoteAndSchedule>>

    @Query("SELECT COUNT(*) FROM Note")
    abstract fun getNotesCount(): LiveData<Int>

    @Insert
    protected abstract suspend fun insertNote(note: Note): Long

    @Insert
    protected abstract suspend fun insertSchedule(schedule: Schedule)

    @Query("DELETE FROM Schedule")
    protected abstract suspend fun deleteAllSchedules()

    @Query("DELETE FROM Note")
    protected abstract suspend fun deleteAllNotes()

    /**
     * Insère une note et son éventuel planning dans une seule transaction,
     * afin que les observateurs ne voient jamais la note sans son planning.
     */
    @Transaction
    open suspend fun insertNoteWithSchedule(note: Note, schedule: Schedule?) {
        val noteId = insertNote(note)
        schedule?.let { insertSchedule(it.copy(ownerId = noteId)) }
    }

    /** Supprime les plannings puis les notes de manière atomique. */
    @Transaction
    open suspend fun deleteAll() {
        deleteAllSchedules()
        deleteAllNotes()
    }
}
