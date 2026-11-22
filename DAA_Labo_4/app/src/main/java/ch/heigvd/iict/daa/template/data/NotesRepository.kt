package ch.heigvd.iict.daa.template.data

import androidx.lifecycle.LiveData
import ch.heigvd.iict.daa.template.models.Note
import ch.heigvd.iict.daa.template.models.NoteAndSchedule

class NotesRepository(private val dao: NoteDao) {
    val allNotes: LiveData<List<NoteAndSchedule>> = dao.getAllNotes()
    val countNotes: LiveData<Int> = dao.getNotesCount()

    suspend fun insertNote(note: Note) {
        dao.insertNote(note)
    }

    suspend fun generateNote() {
        val note = Note.generateRandomNote()
        val noteId = dao.insertNote(note)
        val schedule = Note.generateRandomSchedule()
        schedule?.let { dao.insertSchedule(it.copy(ownerId = noteId)) }
    }

    suspend fun deleteAll() {
        dao.deleteAllSchedules()
        dao.deleteAllNotes()
    }
}
