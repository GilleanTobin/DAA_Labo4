/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template

import android.app.Application
import ch.heigvd.iict.daa.template.data.NotesDatabase
import ch.heigvd.iict.daa.template.data.NotesRepository
import ch.heigvd.iict.daa.template.data.SortPreferences

/** Application surchargée pour créer et conserver une unique instance du Repository. */
class NotesApplication : Application() {
    val repository: NotesRepository by lazy {
        NotesRepository(
            NotesDatabase.getDatabase(this).noteDao(),
            SortPreferences(this)
        )
    }
}
