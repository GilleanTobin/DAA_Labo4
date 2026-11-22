/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ch.heigvd.iict.daa.template.models.SortOption
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModel
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModelFactory

/**
 * Activité principale. Ses layouts (layout/ et layout-sw600dp-land/) déclarent les fragments :
 * liste des notes seule sur smartphone, liste + contrôles sur tablette en paysage.
 */
class MainActivity : AppCompatActivity() {
    // Le ViewModel est lié à l'Activité ; les fragments récupèrent la même instance
    private val viewModel: NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<MaterialToolbar>(R.id.top_toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Sur tablette, ces actions sont proposées par ControlsFragment
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        menu.findItem(R.id.action_generate)?.isVisible = !isTablet
        menu.findItem(R.id.action_delete)?.isVisible = !isTablet
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort_creation -> viewModel.setSortOption(SortOption.CREATION)
            R.id.action_sort_schedule -> viewModel.setSortOption(SortOption.SCHEDULE)
            R.id.action_generate -> viewModel.generateANote()
            R.id.action_delete -> viewModel.deleteAllNote()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
