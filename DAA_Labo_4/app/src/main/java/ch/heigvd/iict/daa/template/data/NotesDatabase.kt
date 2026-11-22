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
        @Volatile
        private var instance: NotesDatabase? = null

        fun getDatabase(context: Context): NotesDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_Schedule_ownerId` " +
                        "ON `Schedule` (`ownerId`)"
                )
            }
        }
    }
}
