/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.daa.template.data.NotesRepository
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import ch.heigvd.iict.daa.template.models.SortOption
import kotlinx.coroutines.launch

/**
 * ViewModel partagé par MainActivity, NotesFragment et ControlsFragment.
 * Il ne référence aucun élément du cycle de vie Android (Activity, Fragment, View, Context).
 */
class NotesViewModel(private val repository: NotesRepository) : ViewModel() {
    val allNotes: LiveData<List<NoteAndSchedule>> = repository.allNotes
    val countNotes: LiveData<Int> = repository.countNotes

    // Option de tri persistée (DataStore), exposée en LiveData pour l'interface
    val sortOption: LiveData<SortOption> = repository.sortOption.asLiveData()

    fun setSortOption(option: SortOption) {
        viewModelScope.launch { repository.setSortOption(option) }
    }

    fun generateANote() {
        viewModelScope.launch { repository.generateNote() }
    }

    fun deleteAllNote() {
        viewModelScope.launch { repository.deleteAll() }
    }
}
