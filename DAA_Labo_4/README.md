# DAA — Laboratoire 4

Application Android de gestion de notes, réalisée avec Kotlin, MVVM, Room 3 et RecyclerView.

## Fonctionnalités

- Affichage des notes et de leur éventuel planning.
- Tri par date de création ou date prévue; le choix est conservé avec DataStore.
- Génération d’une note aléatoire et suppression de toutes les notes.
- Sur tablette en paysage (`sw600dp-land`), liste et contrôles dans deux fragments côte à côte; sur téléphone, les actions sont accessibles depuis le menu.

## Architecture

- **Données :** `NoteDao`, `NotesDatabase` et `NotesRepository`.
- **Interface :** `MainActivity`, `NotesFragment`, `ControlsFragment` et `NotesAdapter`.
- **État :** `NotesViewModel` (LiveData des notes, du compteur et de l’option de tri). Le tri est appliqué par `NotesAdapter`.
- **Base locale :** Room 3. La relation note-planning est limitée à zéro ou un planning par note par un index unique sur `Schedule.ownerId`. Le schéma versionné est dans `app/schemas/`; la migration 1→2 ajoute cet index.

## Compilation

Depuis la racine du projet, lancer :

```powershell
.\gradlew.bat assembleDebug
```

L’APK debug est créé dans `app/build/outputs/apk/debug/`.
