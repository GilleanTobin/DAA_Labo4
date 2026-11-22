/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.heigvd.iict.daa.template.NotesApplication
import ch.heigvd.iict.daa.template.R
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModel
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModelFactory

/**
 * Fragment de contrôle (tablette uniquement) : compteur de notes et boutons
 * remplaçant les entrées "générer" et "supprimer" du menu.
 */
class ControlsFragment : Fragment() {
    private val viewModel: NotesViewModel by activityViewModels {
        NotesViewModelFactory((requireActivity().application as NotesApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_controls, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val count = view.findViewById<TextView>(R.id.notes_count)
        view.findViewById<Button>(R.id.generate_note).setOnClickListener {
            viewModel.generateANote()
        }
        view.findViewById<Button>(R.id.delete_notes).setOnClickListener {
            viewModel.deleteAllNote()
        }
        // Le compteur est mis à jour automatiquement par Room à chaque modification
        viewModel.countNotes.observe(viewLifecycleOwner) {
            count.text = getString(R.string.note_count, it)
        }
    }
}
