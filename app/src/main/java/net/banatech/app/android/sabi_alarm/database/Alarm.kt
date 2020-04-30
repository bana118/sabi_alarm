package net.banatech.app.android.sabi_alarm.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alarm(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "hour") var hour: Int,
    @ColumnInfo(name = "minute") var minute: Int,
    @ColumnInfo(name = "time_text") var timeText: String,
    @ColumnInfo(name = "is_boolean") val isVibration: Boolean,
    @ColumnInfo(name = "is_repeatable") var isRepeatable: Boolean,
    @ColumnInfo(name = "boolean_array") val weekAlarmList: BooleanArray,
    @ColumnInfo(name = "sound_file_name") val soundFileName: String,
    @ColumnInfo(name = "sound_start_time") val soundStartTime: Int,
    @ColumnInfo(name = "is_default_sound") val isDefaultSound: Boolean){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Alarm

        if (uid != other.uid) return false
        if (hour != other.hour) return false
        if (minute != other.minute) return false
        if (timeText != other.timeText) return false
        if (isVibration != other.isVibration) return false
        if (isRepeatable != other.isRepeatable) return false
        if (!weekAlarmList.contentEquals(other.weekAlarmList)) return false
        if (soundFileName != other.soundFileName) return false
        if (soundStartTime != other.soundStartTime) return false
        if (isDefaultSound != other.isDefaultSound) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid
        result = 31 * result + hour
        result = 31 * result + minute
        result = 31 * result + timeText.hashCode()
        result = 31 * result + isVibration.hashCode()
        result = 31 * result + isRepeatable.hashCode()
        result = 31 * result + weekAlarmList.contentHashCode()
        result = 31 * result + soundFileName.hashCode()
        result = 31 * result + soundStartTime
        result = 31 * result + isDefaultSound.hashCode()
        return result
    }
}
