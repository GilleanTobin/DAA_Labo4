package ch.heigvd.iict.daa.template.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ch.heigvd.iict.daa.template.models.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sortDataStore by preferencesDataStore(name = "sort_preferences")
private val SORT_OPTION = stringPreferencesKey("sort_option")

class SortPreferences(private val context: Context) {
    val sortOption: Flow<SortOption> = context.sortDataStore.data.map { preferences ->
        runCatching {
            SortOption.valueOf(preferences[SORT_OPTION] ?: SortOption.CREATION.name)
        }.getOrDefault(SortOption.CREATION)
    }

    suspend fun setSortOption(option: SortOption) {
        context.sortDataStore.edit { preferences ->
            preferences[SORT_OPTION] = option.name
        }
    }
}
