package net.banatech.app.android.sabi_alarm.sound.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Sound::class], version = 1)
abstract class SoundDatabase : RoomDatabase() {
    abstract fun soundDao(): SoundDao

    companion object {
        private const val dbName = "sabi_alarm_sounds.db"
        private var instance: SoundDatabase? = null

        fun getInstance(context: Context): SoundDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, SoundDatabase::class.java, dbName)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return requireNotNull(instance)
        }
    }
}