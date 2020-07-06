package net.banatech.app.android.sabi_alarm.sound.database

import androidx.room.*

@Dao
interface SoundDao {
    @Query("SELECT * FROM sounds")
    suspend fun getAll(): List<Sound>

    @Query("SELECT * FROM sounds WHERE id IN (:soundIds)")
    suspend fun loadAllByIds(soundIds: IntArray): List<Sound>

    @Insert
    suspend fun insertAll(vararg sounds: Sound)

    @Update
    suspend fun update(vararg sounds: Sound)

    @Delete
    suspend fun delete(sound: Sound)
}
