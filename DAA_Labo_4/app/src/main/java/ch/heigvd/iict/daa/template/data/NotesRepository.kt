/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.data

import androidx.lifecycle.LiveData
import ch.heigvd.iict.daa.template.models.Note
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import ch.heigvd.iict.daa.template.models.SortOption
import kotlinx.coroutines.flow.Flow

/**
 * Point d'accès unique aux données : notes (Room) et option de tri (DataStore).
 * Le ViewModel ne connaît que le Repository, jamais le Context ni la base.
 */
class NotesRepository(
    private val dao: NoteDao,
    private val sortPreferences: SortPreferences
) {
    val allNotes: LiveData<List<NoteAndSchedule>> = dao.getAllNotes()
    val countNotes: LiveData<Int> = dao.getNotesCount()
    val sortOption: Flow<SortOption> = sortPreferences.sortOption

    /** Génère une note aléatoire, avec ou sans planning, et l'insère. */
    suspend fun generateNote() {
        dao.insertNoteWithSchedule(Note.generateRandomNote(), Note.generateRandomSchedule())
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun setSortOption(option: SortOption) {
        sortPreferences.setSortOption(option)
    }
}
