# DAA – Laboratoire 4 : MVVM, Room et RecyclerView

**Auteurs :** Yann Mermoud, Julien Gorgerat, Mauro Dos Santos

---

## 1. Implémentation

### 1.1 Architecture générale

L'application suit l'architecture MVVM présentée en cours :

```text
MainActivity ─┬─ NotesFragment (RecyclerView + NotesAdapter)
              └─ ControlsFragment (tablette uniquement)
                       │  observent les LiveData / appellent les méthodes
                       ▼
                 NotesViewModel  (partagé via viewModels / activityViewModels)
                       │
                       ▼
                 NotesRepository ─┬─ NoteDao ── NotesDatabase (Room 3)
                                  └─ SortPreferences (DataStore)
```

- `NotesApplication` surcharge `Application` et crée une unique instance du `NotesRepository` (initialisation paresseuse).
- `NotesViewModel` est créé par `NotesViewModelFactory`, qui lui injecte le repository. L'Activité l'obtient avec `by viewModels`, les deux fragments avec `by activityViewModels` : les trois composants partagent donc **la même instance**.
- Le ViewModel ne contient aucune référence vers une Activité, un Fragment, une View ou l'Application. Il ne connaît que le repository.

### 1.2 Base de données Room

- **Entités :** `Note` et `Schedule` sont celles fournies. Nous avons ajouté un **index unique** sur `Schedule.ownerId` afin que la base garantisse elle-même la relation One-to-One facultative (0..1). Une note ne peut ainsi pas avoir deux plannings. Cette modification a fait passer la base en version 2. La migration `MIGRATION_1_2` crée l'index pour les bases existantes, et le schéma est exporté dans `app/schemas/`.
- **Convertisseurs :** `Converters` convertit les `Calendar` en `Long` (timestamp en ms). Les énumérations `State` et `Type` sont stockées par Room sous forme de texte.
- **DAO :** `NoteDao` expose `getAllNotes()` (`LiveData<List<NoteAndSchedule>>`, annoté `@Transaction` car la relation implique deux requêtes) et `getNotesCount()` (`LiveData<Int>`). Les écritures composées sont des méthodes `@Transaction` :
  - `insertNoteWithSchedule()` insère la note, puis son planning avec l'identifiant généré. Les observateurs ne voient donc jamais une note sans le planning qui l'accompagne.
  - `deleteAll()` supprime les plannings puis les notes de manière atomique.
- **populate() :** un `RoomDatabase.Callback` génère 10 notes aléatoires à la création de la base. Il écrit directement via la `SQLiteConnection` reçue, car le DAO n'est pas utilisable pendant l'ouverture de la base.

### 1.3 ViewModel

```kotlin
class NotesViewModel(private val repository: NotesRepository) : ViewModel() {
    val allNotes: LiveData<List<NoteAndSchedule>> = repository.allNotes
    val countNotes: LiveData<Int> = repository.countNotes
    val sortOption: LiveData<SortOption> = repository.sortOption.asLiveData()

    fun setSortOption(option: SortOption) { viewModelScope.launch { repository.setSortOption(option) } }
    fun generateANote() { viewModelScope.launch { repository.generateNote() } }
    fun deleteAllNote() { viewModelScope.launch { repository.deleteAll() } }
}
```

Les écritures sont des fonctions `suspend` lancées dans `viewModelScope`. Room les exécute hors du thread principal et elles sont annulées automatiquement si le ViewModel est détruit.

### 1.4 Interface et form factors

- **Smartphone** (`layout/activity_main.xml`) : une toolbar et `NotesFragment`. Le menu propose le tri (sous-menu « Options de tri »), la génération d'une note et la suppression de toutes les notes.
- **Tablette** (`layout-sw600dp-land/activity_main.xml`) : `NotesFragment` à gauche et `ControlsFragment` à droite, qui affiche le compteur (`countNotes`) et les boutons « générer » et « supprimer ». Comme demandé, ce layout **combine une contrainte de taille (`sw600dp`) et d'orientation (`land`)**. Une tablette en portrait utilise le layout smartphone.
- La ressource booléenne `is_tablet` utilise exactement les mêmes qualificateurs (`values-sw600dp-land`). Dans `onPrepareOptionsMenu`, l'Activité masque les entrées « générer » et « supprimer » lorsque `ControlsFragment` est affiché, ce qui garantit que menu et layout restent cohérents.

### 1.5 RecyclerView et tri

- `NotesAdapter` hérite de `ListAdapter` avec un `DiffUtil.ItemCallback`. Seules les lignes réellement modifiées sont redessinées, et l'insertion d'une note est animée.
- **Deux layouts** : `item_note_simple` et `item_note_schedule`, sélectionnés dans `getItemViewType()` selon la présence d'un `Schedule`.
- Tous les champs sont affichés : icône selon le type, titre, texte, type, état (teinté en vert si `DONE`, en orange si `IN_PROGRESS`), date de création et, le cas échéant, date prévue. Le `ViewHolder` recherche ses vues une seule fois, à sa création.
- **Le tri est implémenté dans l'Adapter**, comme demandé. `NotesFragment` observe `allNotes` et `sortOption` et les transmet à l'adapter (`setNotes()` / `setSortOption()`), qui trie puis appelle `submitList()`.
  - *Par date de création* : les plus récentes en premier.
  - *Par date prévue* : l'échéance la plus proche en premier. Les notes sans planning sont placées à la fin, triées par date de création.

---

## 2. Questions complémentaires

### 6.1 Sauvegarde de l'option de tri

> Quelle est la meilleure approche pour sauver, même après la fermeture de l'app, le choix de l'option de tri de la liste des notes ?

Il s'agit d'une petite préférence clé/valeur, qui n'a ni relation ni besoin de requêtes. L'outil recommandé par Android est **Jetpack DataStore (Preferences)** :

- `onSaveInstanceState` et le `ViewModel` ne survivent pas à la fermeture de l'application ou à la mort du processus. Ils ne répondent donc pas à la question.
- Room serait surdimensionné : il faudrait créer une table pour une seule valeur.
- `SharedPreferences` fonctionne, mais son API est synchrone (lecture sur le thread principal au premier accès, `commit()` bloquant). Les erreurs d'E/S n'y sont pas signalées et il n'y a pas de notification simple des changements. DataStore est son remplaçant officiel : les accès sont asynchrones (coroutines), les écritures sont transactionnelles, et les valeurs sont exposées sous forme de `Flow` observable.

Nous enregistrons le nom de l'option (`CREATION` ou `SCHEDULE`). La lecture retombe sur `CREATION` si la valeur est absente ou invalide :

```kotlin
private val Context.sortDataStore by preferencesDataStore(name = "sort_preferences")
private val SORT_OPTION = stringPreferencesKey("sort_option")

class SortPreferences(context: Context) {
    private val appContext = context.applicationContext

    val sortOption: Flow<SortOption> = appContext.sortDataStore.data.map { preferences ->
        runCatching {
            SortOption.valueOf(preferences[SORT_OPTION] ?: SortOption.CREATION.name)
        }.getOrDefault(SortOption.CREATION)
    }

    suspend fun setSortOption(option: SortOption) {
        appContext.sortDataStore.edit { it[SORT_OPTION] = option.name }
    }
}
```

`SortPreferences` est accessible via le repository, et le ViewModel l'expose avec `repository.sortOption.asLiveData()`. Le ViewModel ne manipule donc jamais de `Context`. Au redémarrage, le DataStore émet la valeur sauvegardée et l'adapter retrie la liste.

### 6.2 Limites de LiveData

> L'accès à la liste des notes issues de la base de données Room se fait avec une LiveData. Est-ce que cette solution présente des limites ?

Oui, plusieurs :

1. **Couplage à Android et au thread principal.** LiveData fait partie des composants Android et livre ses valeurs uniquement sur le thread principal. Elle est conçue pour la couche UI. L'utiliser dans le repository ou la couche de données lie ces couches à Android, les rend difficiles à tester en JVM pur (il faut `InstantTaskExecutorRule`) et empêche leur réutilisation (p.ex. Kotlin Multiplatform, que Room 3 cible).
2. **Peu d'opérateurs.** LiveData n'offre que `map`, `switchMap`, `distinctUntilChanged` et `MediatorLiveData`. Combiner plusieurs sources (notes + option de tri), filtrer, faire du debounce ou gérer les erreurs devient vite verbeux. Les coroutines/Flow offrent `combine`, `flatMapLatest`, `catch`, `debounce`, etc.
3. **La liste complète est rechargée à chaque modification.** Room invalide la requête dès qu'une des tables observées (`Note` ou `Schedule`) change. Toute la liste est alors relue et renvoyée, puis retriée et comparée côté UI. Avec quelques centaines de notes, ce n'est pas un problème. Avec des milliers, cela coûte en mémoire et en CPU.
4. **Pas de gestion explicite des états de chargement et d'erreur.** On ne peut pas distinguer facilement « pas encore chargé » de « liste vide », et une exception dans la requête n'a pas de canal dédié.

**Approches mieux adaptées :**

- Faire retourner un **`Flow<List<NoteAndSchedule>>`** au DAO, et le convertir dans le ViewModel en **`StateFlow`** avec `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())`. Le tri peut alors s'exprimer par `combine(notesFlow, sortFlow) { … }`. L'UI collecte avec `repeatOnLifecycle(Lifecycle.State.STARTED)` (ou `collectAsStateWithLifecycle()` avec Compose), ce qui conserve le respect du cycle de vie.
- Pour de grandes listes, utiliser **Paging 3** : le DAO retourne un `PagingSource<Int, NoteAndSchedule>`, le ViewModel expose un `Flow<PagingData>` (`cachedIn(viewModelScope)`) et la RecyclerView utilise un `PagingDataAdapter`. Seules les pages visibles sont chargées. Le tri doit alors être fait **dans la requête SQL** (`ORDER BY`) plutôt qu'en mémoire.

### 6.3 Sélection d'une note pour l'éditer

> Comment procéderiez-vous si vous souhaitiez proposer une interface permettant de sélectionner une note pour l'éditer ?

1. **Callback dans l'adapter.** L'adapter reçoit une lambda dans son constructeur. Le `ViewHolder` enregistre un `OnClickListener` sur `itemView` une seule fois, à sa création, et utilise `bindingAdapterPosition` pour retrouver l'élément cliqué :

   ```kotlin
   class NotesAdapter(
       private val onNoteClick: (NoteAndSchedule) -> Unit
   ) : ListAdapter<NoteAndSchedule, NoteViewHolder>(DIFF) {

       override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
           val holder = NoteViewHolder(/* … */)
           holder.itemView.setOnClickListener {
               val pos = holder.bindingAdapterPosition
               if (pos != RecyclerView.NO_POSITION) onNoteClick(getItem(pos))
           }
           return holder
       }
   }
   ```

   Ajouter aussi un retour visuel (`android:foreground="?attr/selectableItemBackground"` sur la carte, ou `MaterialCardView` avec `android:clickable="true"`).

2. **Navigation vers l'édition.** Dans `NotesFragment` :
   `NotesAdapter { note -> viewModel.selectNote(note.note.noteId) }`.
   - Sur smartphone, on ouvre un `NoteEditFragment` (ou un `DialogFragment`) en lui passant uniquement le `noteId` en argument. On ne passe pas l'objet entier : il serait obsolète et un `Bundle` a une taille limitée.
   - Sur tablette, on pourrait afficher l'éditeur dans le panneau de droite (pattern liste/détail).

3. **Données.** On ajoute au DAO `@Query("SELECT * FROM Note WHERE noteId = :id") fun getNote(id: Long): LiveData<NoteAndSchedule>`, ainsi que des méthodes `@Update` pour `Note` et `@Upsert`/`@Delete` pour `Schedule`. Le ViewModel d'édition charge la note, expose les champs modifiables et persiste les changements avec `viewModelScope.launch { repository.update(…) }`. La liste se met à jour automatiquement grâce à l'observation de Room.

4. **Sélection multiple** (p.ex. pour supprimer plusieurs notes) : la bibliothèque **`androidx.recyclerview:recyclerview-selection`** (`SelectionTracker`, `ItemKeyProvider` basé sur `noteId`, `ItemDetailsLookup`) gère l'appui long, la sélection, l'état visuel et sa sauvegarde lors d'une rotation, sans réimplémenter la détection des gestes.

Il ne faut **pas** calculer la position du doigt sur la RecyclerView pour en déduire l'élément touché (`addOnItemTouchListener` + `findChildViewUnder`). C'est fragile, ce n'est pas accessible (TalkBack, clavier) et c'est inutile puisque chaque `ViewHolder` connaît déjà sa position.
