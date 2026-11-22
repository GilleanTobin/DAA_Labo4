/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.daa.template.R
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import ch.heigvd.iict.daa.template.models.SortOption
import ch.heigvd.iict.daa.template.models.State
import ch.heigvd.iict.daa.template.models.Type
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter de la liste des notes.
 * - deux layouts : note simple / note associée à un Schedule ;
 * - implémente le tri selon l'option choisie dans le menu ;
 * - ListAdapter + DiffUtil : seules les lignes modifiées sont redessinées.
 */
class NotesAdapter : ListAdapter<NoteAndSchedule, NotesAdapter.NoteViewHolder>(DIFF) {
    private var notes: List<NoteAndSchedule> = emptyList()
    private var sortOption = SortOption.CREATION
    // Instance par adapter (recréé avec la vue) pour suivre un changement de langue
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun setNotes(values: List<NoteAndSchedule>) {
        notes = values
        refresh()
    }

    fun setSortOption(option: SortOption) {
        sortOption = option
        refresh()
    }

    private fun refresh() = submitList(sort(notes, sortOption))

    /**
     * CREATION : plus récentes en premier.
     * SCHEDULE : date prévue la plus proche en premier, notes sans planning à la fin.
     */
    private fun sort(values: List<NoteAndSchedule>, option: SortOption): List<NoteAndSchedule> =
        when (option) {
            SortOption.CREATION ->
                values.sortedByDescending { it.note.creationDate.timeInMillis }
            SortOption.SCHEDULE ->
                values.sortedWith(
                    compareBy<NoteAndSchedule> {
                        it.schedule?.date?.timeInMillis ?: Long.MAX_VALUE
                    }.thenByDescending { it.note.creationDate.timeInMillis }
                )
        }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).schedule == null) VIEW_SIMPLE else VIEW_SCHEDULE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val layout = if (viewType == VIEW_SIMPLE) R.layout.item_note_simple else R.layout.item_note_schedule
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return NoteViewHolder(view, dateFormat)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** ViewHolder commun : les vues sont recherchées une seule fois, à la création. */
    class NoteViewHolder(
        view: View,
        private val dateFormat: SimpleDateFormat
    ) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.note_icon)
        private val title: TextView = view.findViewById(R.id.note_title)
        private val text: TextView = view.findViewById(R.id.note_text)
        private val type: TextView = view.findViewById(R.id.note_type)
        private val state: TextView = view.findViewById(R.id.note_state)
        private val creation: TextView = view.findViewById(R.id.note_creation)
        // Absente du layout des notes simples
        private val schedule: TextView? = view.findViewById(R.id.note_schedule)

        fun bind(item: NoteAndSchedule) {
            val context = itemView.context
            val note = item.note

            title.text = note.title
            text.text = note.text
            type.text = note.type.name
            state.text = note.state.name
            creation.text = context.getString(
                R.string.creation_date,
                dateFormat.format(note.creationDate.time)
            )

            icon.setImageResource(typeIcon(note.type))
            icon.contentDescription = note.type.name

            // Teinte selon l'état de la note
            val stateColor = if (note.state == State.DONE) R.color.state_done else R.color.state_in_progress
            state.setTextColor(ContextCompat.getColor(context, stateColor))

            item.schedule?.let {
                schedule?.text = context.getString(
                    R.string.scheduled_date,
                    dateFormat.format(it.date.time)
                )
            }
        }

        private fun typeIcon(type: Type): Int = when (type) {
            Type.TODO -> R.drawable.todo
            Type.SHOPPING -> R.drawable.shopping
            Type.WORK -> R.drawable.work
            Type.FAMILY -> R.drawable.family
            Type.NONE -> R.drawable.note
        }
    }

    companion object {
        private const val VIEW_SIMPLE = 0
        private const val VIEW_SCHEDULE = 1

        // Deux éléments représentent la même note si leur identifiant est identique
        private val DIFF = object : DiffUtil.ItemCallback<NoteAndSchedule>() {
            override fun areItemsTheSame(oldItem: NoteAndSchedule, newItem: NoteAndSchedule) =
                oldItem.note.noteId == newItem.note.noteId

            override fun areContentsTheSame(oldItem: NoteAndSchedule, newItem: NoteAndSchedule) =
                oldItem == newItem
        }
    }
}
