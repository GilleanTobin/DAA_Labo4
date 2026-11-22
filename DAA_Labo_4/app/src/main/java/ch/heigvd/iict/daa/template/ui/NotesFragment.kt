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

class NotesFragment : Fragment() {
    private val viewModel: NotesViewModel by activityViewModels {
        NotesViewModelFactory((requireActivity().application as NotesApplication).repository)
    }
    private val adapter = NotesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_notes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.notes_recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewModel.sortedNotes.observe(viewLifecycleOwner) { adapter.submit(it) }
    }
}
