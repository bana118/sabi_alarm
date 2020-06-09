package net.banatech.app.android.sabi_alarm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Alarm::class], version = 1)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        private const val dbName = "sabi_alarm.db"
        private var instance: AlarmDatabase? = null

        fun getInstance(context: Context): AlarmDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, AlarmDatabase::class.java, dbName)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return requireNotNull(instance)
        }
    }
}