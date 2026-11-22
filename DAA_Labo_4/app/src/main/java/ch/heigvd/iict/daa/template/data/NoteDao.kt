package ch.heigvd.iict.daa.template.data

import androidx.lifecycle.LiveData
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import ch.heigvd.iict.daa.template.models.Note
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import ch.heigvd.iict.daa.template.models.Schedule

@Dao
interface NoteDao {
    @Transaction
    @Query("SELECT * FROM Note")
    fun getAllNotes(): LiveData<List<NoteAndSchedule>>

    @Query("SELECT COUNT(*) FROM Note")
    fun getNotesCount(): LiveData<Int>

    @Insert
    suspend fun insertNote(note: Note): Long

    @Insert
    suspend fun insertSchedule(schedule: Schedule)

    @Query("DELETE FROM Schedule")
    suspend fun deleteAllSchedules()

    @Query("DELETE FROM Note")
    suspend fun deleteAllNotes()
}
