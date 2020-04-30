package net.banatech.app.android.sabi_alarm.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm")
    fun getAll(): List<Alarm>

    @Query("SELECT * FROM alarm WHERE uid IN (:alarmIds)")
    fun loadAllByIds(alarmIds: IntArray): List<Alarm>

    @Insert
    fun insertAll(vararg alarms: Alarm)

    @Delete
    fun delete(alarm: Alarm)
}