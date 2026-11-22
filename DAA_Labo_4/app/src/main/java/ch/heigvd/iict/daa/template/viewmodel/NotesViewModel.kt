package ch.heigvd.iict.daa.template.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.daa.template.data.NotesRepository
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import ch.heigvd.iict.daa.template.models.SortOption
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NotesRepository) : ViewModel() {
    val allNotes: LiveData<List<NoteAndSchedule>> = repository.allNotes
    val countNotes: LiveData<Int> = repository.countNotes

    private val sortOption = MutableLiveData(SortOption.CREATION)
    private var latestNotes: List<NoteAndSchedule> = emptyList()

    val sortedNotes: LiveData<List<NoteAndSchedule>> = MediatorLiveData<List<NoteAndSchedule>>().apply {
        addSource(repository.allNotes) { notes ->
            latestNotes = notes
            value = sortNotes(notes, sortOption.value ?: SortOption.CREATION)
        }
        addSource(sortOption) { option ->
            value = sortNotes(latestNotes, option ?: SortOption.CREATION)
        }
    }

    fun sortNotes(option: SortOption) {
        sortOption.value = option
    }

    fun generateANote() {
        viewModelScope.launch { repository.generateNote() }
    }

    fun deleteAllNote() {
        viewModelScope.launch { repository.deleteAll() }
    }

    private fun sortNotes(
        notes: List<NoteAndSchedule>,
        option: SortOption
    ): List<NoteAndSchedule> =
        when (option) {
            SortOption.CREATION ->
                notes.sortedByDescending { it.note.creationDate.timeInMillis }
            SortOption.SCHEDULE ->
                notes.sortedWith(
                    compareBy<NoteAndSchedule> {
                        it.schedule?.date?.timeInMillis ?: Long.MAX_VALUE
                    }.thenByDescending { it.note.creationDate.timeInMillis }
                )
        }
}
