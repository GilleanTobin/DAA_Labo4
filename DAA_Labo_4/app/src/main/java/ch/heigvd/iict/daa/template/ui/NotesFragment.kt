/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.daa.template.NotesApplication
import ch.heigvd.iict.daa.template.R
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModel
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModelFactory

/** Fragment affichant la liste des notes dans une RecyclerView. */
class NotesFragment : Fragment() {
    // activityViewModels : même instance que l'Activité et ControlsFragment
    private val viewModel: NotesViewModel by activityViewModels {
        NotesViewModelFactory((requireActivity().application as NotesApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_notes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Adapter créé avec la vue pour ne pas garder une référence vers une ancienne RecyclerView
        val adapter = NotesAdapter()
        val recycler = view.findViewById<RecyclerView>(R.id.notes_recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewModel.allNotes.observe(viewLifecycleOwner) { adapter.setNotes(it) }
        viewModel.sortOption.observe(viewLifecycleOwner) { adapter.setSortOption(it) }
    }
}
