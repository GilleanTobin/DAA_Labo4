/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ch.heigvd.iict.daa.template.models.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Une seule instance de DataStore par fichier et par processus (délégué au niveau du Context)
private val Context.sortDataStore by preferencesDataStore(name = "sort_preferences")
private val SORT_OPTION = stringPreferencesKey("sort_option")

/**
 * Persistance de l'option de tri avec Preferences DataStore,
 * afin qu'elle survive à la fermeture de l'application.
 */
class SortPreferences(context: Context) {
    // On garde le contexte applicatif pour éviter toute fuite d'Activity
    private val appContext = context.applicationContext

    val sortOption: Flow<SortOption> = appContext.sortDataStore.data.map { preferences ->
        // Valeur absente ou inconnue (p.ex. enum renommé) : tri par création par défaut
        runCatching {
            SortOption.valueOf(preferences[SORT_OPTION] ?: SortOption.CREATION.name)
        }.getOrDefault(SortOption.CREATION)
    }

    suspend fun setSortOption(option: SortOption) {
        appContext.sortDataStore.edit { preferences ->
            preferences[SORT_OPTION] = option.name
        }
    }
}
