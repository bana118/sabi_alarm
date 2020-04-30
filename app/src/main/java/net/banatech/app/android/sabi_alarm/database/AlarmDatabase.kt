package net.banatech.app.android.sabi_alarm.database

import androidx.room.Database

@Database(entities = [Alarm::class], version = 1)
abstract class AlarmDatabase {
    abstract fun alarmDao(): AlarmDao
}