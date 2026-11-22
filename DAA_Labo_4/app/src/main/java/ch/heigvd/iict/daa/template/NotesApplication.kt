package ch.heigvd.iict.daa.template

import android.app.Application
import ch.heigvd.iict.daa.template.data.NotesDatabase
import ch.heigvd.iict.daa.template.data.NotesRepository

class NotesApplication : Application() {
    val repository: NotesRepository by lazy {
        NotesRepository(NotesDatabase.getDatabase(this).noteDao())
    }
}
