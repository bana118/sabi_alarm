package net.banatech.app.android.sabi_alarm.alarm.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "hour") var hour: Int,
    @ColumnInfo(name = "minute") var minute: Int,
    @ColumnInfo(name = "time_text") var timeText: String,
    @ColumnInfo(name = "enable") var enable: Boolean,
    @ColumnInfo(name = "is_show_detail") var isShowDetail: Boolean,
    @ColumnInfo(name = "is_boolean") var isVibration: Boolean,
    @ColumnInfo(name = "is_repeatable") var isRepeatable: Boolean,
    @ColumnInfo(name = "is_sunday_alarm") var isSundayAlarm: Boolean,
    @ColumnInfo(name = "is_monday_alarm") var isMondayAlarm: Boolean,
    @ColumnInfo(name = "is_tuesday_alarm") var isTuesdayAlarm: Boolean,
    @ColumnInfo(name = "is_wednesday_alarm") var isWednesdayAlarm: Boolean,
    @ColumnInfo(name = "is_thursday_alarm") var isThursdayAlarm: Boolean,
    @ColumnInfo(name = "is_friday_alarm") var isFridayAlarm: Boolean,
    @ColumnInfo(name = "is_saturday_alarm") var isSaturdayAlarm: Boolean,
    @ColumnInfo(name = "sound_file_name") var soundFileName: String,
    @ColumnInfo(name = "sound_start_time") var soundStartTime: Int, //Milli
    @ColumnInfo(name = "sound_start_time_text") var soundStartTimeText: String,
    @ColumnInfo(name = "is_default_sound") var isDefaultSound: Boolean
)
