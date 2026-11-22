/*
 * DAA - Laboratoire 4
 * Auteurs : Yann Mermoud, Julien Gorgerat, Mauro Dos Santos
 */
package ch.heigvd.iict.daa.template.data

import android.content.Context
import androidx.room3.migration.Migration
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import androidx.room3.livedata.LiveDataDaoReturnTypeConverter
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import ch.heigvd.iict.daa.template.models.Note
import ch.heigvd.iict.daa.template.models.Schedule

/**
 * Base de données Room de l'application (singleton).
 * Version 2 : ajout d'un index unique sur Schedule.ownerId pour garantir la relation 0..1.
 */
@Database(
    entities = [Note::class, Schedule::class],
    version = 2,
    exportSchema = true
)
@ColumnTypeConverters(Converters::class)
@DaoReturnTypeConverters(LiveDataDaoReturnTypeConverter::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        private const val POPULATE_COUNT = 10

        @Volatile
        private var instance: NotesDatabase? = null

        fun getDatabase(context: Context): NotesDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(populateCallback)
                    .build()
                    .also { instance = it }
            }

        // Migration des bases créées avec la version 1 (sans l'index unique)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_Schedule_ownerId` " +
                        "ON `Schedule` (`ownerId`)"
                )
            }
        }

        // Remplit la base avec des notes de test lors de sa toute première création
        private val populateCallback = object : RoomDatabase.Callback() {
            override suspend fun onCreate(connection: SQLiteConnection) {
                populate(connection)
            }
        }

        /**
         * Génère des notes aléatoires. On passe par la connexion fournie par le callback
         * (et non par le DAO) car la base est encore en cours d'ouverture à ce moment.
         */
        private fun populate(connection: SQLiteConnection) {
            val converters = Converters()
            repeat(POPULATE_COUNT) {
                val note = Note.generateRandomNote()
                connection.prepare(
                    "INSERT INTO Note (state, title, text, creationDate, type) VALUES (?, ?, ?, ?, ?)"
                ).use { stmt ->
                    stmt.bindText(1, note.state.name)
                    stmt.bindText(2, note.title)
                    stmt.bindText(3, note.text)
                    stmt.bindLong(4, converters.calendarToLong(note.creationDate)!!)
                    stmt.bindText(5, note.type.name)
                    stmt.step()
                }
                val noteId = connection.prepare("SELECT last_insert_rowid()").use { stmt ->
                    stmt.step()
                    stmt.getLong(0)
                }
                Note.generateRandomSchedule()?.let { schedule ->
                    connection.prepare(
                        "INSERT INTO Schedule (ownerId, date) VALUES (?, ?)"
                    ).use { stmt ->
                        stmt.bindLong(1, noteId)
                        stmt.bindLong(2, converters.calendarToLong(schedule.date)!!)
                        stmt.step()
                    }
                }
            }
        }
    }
}
