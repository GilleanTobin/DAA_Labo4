package ch.heigvd.iict.daa.template.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.daa.template.R
import ch.heigvd.iict.daa.template.models.NoteAndSchedule
import ch.heigvd.iict.daa.template.models.Type
import java.text.SimpleDateFormat
import java.util.Locale

class NotesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: List<NoteAndSchedule> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun submit(values: List<NoteAndSchedule>) {
        items = values
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position].schedule == null) 0 else 1

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == 0) R.layout.item_note_simple else R.layout.item_note_schedule
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return when (viewType) {
            0 -> SimpleViewHolder(view)
            else -> ScheduleViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val title = holder.itemView.findViewById<TextView>(R.id.note_title)
        val text = holder.itemView.findViewById<TextView>(R.id.note_text)
        val state = holder.itemView.findViewById<TextView>(R.id.note_state)
        val type = holder.itemView.findViewById<TextView>(R.id.note_type)
        val creation = holder.itemView.findViewById<TextView>(R.id.note_creation)
        val icon = holder.itemView.findViewById<ImageView>(R.id.note_icon)

        title.text = item.note.title
        text.text = item.note.text
        state.text = item.note.state.name
        type.text = item.note.type.name
        creation.text = holder.itemView.context.getString(
            R.string.creation_date,
            dateFormat.format(item.note.creationDate.time)
        )
        icon.setImageResource(typeIcon(item.note.type))
        state.setTextColor(
            if (item.note.state.name == "DONE") 0xFF2E7D32.toInt() else 0xFFE65100.toInt()
        )

        if (holder is ScheduleViewHolder) {
            holder.schedule.text = holder.itemView.context.getString(
                R.string.scheduled_date,
                dateFormat.format(item.schedule!!.date.time)
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

    class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val schedule: TextView = view.findViewById(R.id.note_schedule)
    }
}
