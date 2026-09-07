package com.lamndt.smartmovie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        LibraryItemEntity::class,
        LibraryOutboxEntity::class,
        AccountMutationOutboxEntity::class,
        EpisodeWatchEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class SmartMovieDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        fun create(context: Context): SmartMovieDatabase = Room.databaseBuilder(
            context.applicationContext,
            SmartMovieDatabase::class.java,
            "smartmovie_library.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE library_items ADD COLUMN adult INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE library_items ADD COLUMN syncOrigin TEXT NOT NULL DEFAULT 'local'")
                db.execSQL("ALTER TABLE library_items ADD COLUMN favoritePending INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE library_items ADD COLUMN watchlistPending INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE library_items ADD COLUMN remoteRevision TEXT")
                db.execSQL("ALTER TABLE library_items ADD COLUMN accountId INTEGER")
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS library_outbox (
                        mutationId TEXT NOT NULL PRIMARY KEY,
                        libraryKey TEXT NOT NULL,
                        mediaType TEXT NOT NULL,
                        mediaId INTEGER NOT NULL,
                        collection TEXT NOT NULL,
                        enabled INTEGER NOT NULL,
                        accountId INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        attemptCount INTEGER NOT NULL DEFAULT 0,
                        lastAttemptAt INTEGER,
                        lastError TEXT
                    )""".trimIndent(),
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS account_mutation_outbox (
                        mutationId TEXT NOT NULL PRIMARY KEY,
                        accountId INTEGER NOT NULL,
                        payloadJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        attemptCount INTEGER NOT NULL DEFAULT 0,
                        lastAttemptAt INTEGER,
                        lastError TEXT
                    )""".trimIndent(),
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS episode_watch_progress (
                        episodeKey TEXT NOT NULL PRIMARY KEY,
                        seriesId INTEGER NOT NULL,
                        seasonNumber INTEGER NOT NULL,
                        episodeNumber INTEGER NOT NULL,
                        watchedAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )""".trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_episode_watch_progress_seriesId_seasonNumber " +
                        "ON episode_watch_progress(seriesId, seasonNumber)",
                )
            }
        }
    }
}
