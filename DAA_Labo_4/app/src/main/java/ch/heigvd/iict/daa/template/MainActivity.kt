package ch.heigvd.iict.daa.template

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ch.heigvd.iict.daa.template.data.SortPreferences
import ch.heigvd.iict.daa.template.models.SortOption
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModel
import ch.heigvd.iict.daa.template.viewmodel.NotesViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: NotesViewModel by viewModels {
        NotesViewModelFactory((application as NotesApplication).repository)
    }
    private val sortPreferences by lazy { SortPreferences(this) }

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sortPreferences.sortOption.collect { option ->
                    applySort(option)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        menu.findItem(R.id.action_generate)?.isVisible = !isTablet
        menu.findItem(R.id.action_delete)?.isVisible = !isTablet
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort_creation -> saveAndApplySort(SortOption.CREATION)
            R.id.action_sort_schedule -> saveAndApplySort(SortOption.SCHEDULE)
            R.id.action_generate -> viewModel.generateANote()
            R.id.action_delete -> viewModel.deleteAllNote()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun saveAndApplySort(option: SortOption) {
        lifecycleScope.launch { sortPreferences.setSortOption(option) }
        applySort(option)
    }

    private fun applySort(option: SortOption) {
        viewModel.sortNotes(option)
    }
}