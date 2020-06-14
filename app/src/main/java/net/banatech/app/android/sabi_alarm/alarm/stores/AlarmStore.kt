package net.banatech.app.android.sabi_alarm.alarm.stores

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.AlarmBroadcastReceiver
import net.banatech.app.android.sabi_alarm.alarm.actions.Action
import net.banatech.app.android.sabi_alarm.alarm.actions.AlarmActions
import net.banatech.app.android.sabi_alarm.database.Alarm
import org.greenrobot.eventbus.Subscribe
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

object AlarmStore : Store() {
    val alarms: ArrayList<Alarm> = ArrayList()
    private lateinit var lastDeleted: Alarm
    lateinit var selectedAlarm: Alarm
    var canUndo = false

    @Subscribe
    @SuppressWarnings("unchecked")
    override fun onAction(action: Action) {
        when (action.type) {
            AlarmActions.ALARM_CREATE -> {
                val hour = action.data[AlarmActions.KEY_HOUR]
                val minute = action.data[AlarmActions.KEY_MINUTE]
                val context = action.data[AlarmActions.KEY_CONTEXT]
                check(hour is Int && minute is Int) { "Hour and minute value must be Int" }
                check(context is Context) { "Context value must be Context" }
                create(hour, minute, context)
                emitStoreCreate()
            }
            AlarmActions.ALARM_DESTROY -> {
                val id = action.data[AlarmActions.KEY_ID]
                val context = action.data[AlarmActions.KEY_CONTEXT]
                check(id is Int) { "Id value must be Int" }
                check(context is Context) { "Context value must be Context" }
                destroy(id, context)
                emitStoreDestroy()
            }
            AlarmActions.ALARM_UNDO_DESTROY -> {
                val context = action.data[AlarmActions.KEY_CONTEXT]
                check(context is Context) { "Context value must be Context" }
                undoDestroy(context)
                emitStoreChange()
            }
            AlarmActions.ALARM_EDIT -> {
                val id = action.data[AlarmActions.KEY_ID]
                val hour = action.data[AlarmActions.KEY_HOUR]
                val minute = action.data[AlarmActions.KEY_MINUTE]
                val context = action.data[AlarmActions.KEY_CONTEXT]
                check(id is Int) { "Id value must be Int" }
                check(hour is Int && minute is Int) { "Hour and minute value must be Int" }
                check(context is Context) { "Context value must be Context" }
                edit(id, hour, minute, context)
                emitStoreTimeChange()
            }
            AlarmActions.ALARM_ENABLE_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val enable = action.data[AlarmActions.KEY_ENABLE]
                val context = action.data[AlarmActions.KEY_CONTEXT]
                check(id is Int) { "Id value must be Int" }
                check(enable is Boolean) { "Enable value must be Int" }
                check(context is Context) { "Context value must be Context" }
                switchEnable(id, enable, context)
                emitStoreChange()
            }
            AlarmActions.ALARM_IS_SHOW_DETAIL_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val isShowDetail = action.data[AlarmActions.KEY_IS_SHOW_DETAIL_SWITCH]
                check(id is Int) { "Id value must be Int" }
                check(isShowDetail is Boolean) { "IsShowDetail value must be Int" }
                switchDetail(id, isShowDetail)
                emitStoreChange()
            }
            AlarmActions.ALARM_IS_VIBRATION_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val isVibration = action.data[AlarmActions.KEY_IS_VIBRATION]
                check(id is Int) { "Id value must be Int" }
                check(isVibration is Boolean) { "IsVibration value must be Int" }
                switchVibration(id, isVibration)
                emitStoreChange()
            }
            AlarmActions.ALARM_IS_REPEATABLE_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val isReadable = action.data[AlarmActions.KEY_IS_REPEATABLE]
                check(id is Int) { "Id value must be Int" }
                check(isReadable is Boolean) { "IsRepeatable value must be Int" }
                switchRepeatable(id, isReadable)
                emitStoreChange()
            }
            AlarmActions.ALARM_DAY_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val dayOfWeek = action.data[AlarmActions.KEY_DAY_OF_WEEK]
                val dayEnable = action.data[AlarmActions.KEY_DAY_ENABLE]
                check(id is Int) { "Id value must be Int" }
                check(dayOfWeek is Int) { "DayOfWeek value must be Int" }
                check(dayEnable is Boolean) { "DayEnable value must be Boolean" }
                switchWeekEnable(id, dayOfWeek, dayEnable)
                emitStoreChange()
            }
            AlarmActions.ALARM_SOUND_SELECT -> {
                val id = action.data[AlarmActions.KEY_ID]
                val soundFileName = action.data[AlarmActions.KEY_SOUND_FILE_NAME]
                check(id is Int) { "Id value must be Int" }
                check(soundFileName is String) { "SoundFileName value must be String" }
                selectSound(id, soundFileName)
                emitStoreSoundSelect()
            }
        }
    }

    private fun create(hour: Int, minute: Int, context: Context) {
        val id = System.currentTimeMillis().toInt() //TODO unnecessary when using database
        val timeText = String.format("%02d:%02d", hour, minute)
        val alarm = Alarm(
            id = id,
            hour = hour,
            minute = minute,
            timeText = timeText,
            enable = true,
            isShowDetail = false,
            isVibration = false,
            isRepeatable = false,
            isSundayAlarm = false,
            isMondayAlarm = true,
            isTuesdayAlarm = true,
            isWednesdayAlarm = true,
            isThursdayAlarm = true,
            isFridayAlarm = true,
            isSaturdayAlarm = false,
            soundFileName = "beethoven_no5_1st.mp3",
            soundStartTime = 0,
            isDefaultSound = true
        )
        addAlarm(alarm, context)
    }

    private fun destroy(id: Int, context: Context) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                cancelAlarm(alarm, context)
                lastDeleted = alarm.copy()
                canUndo = true
                deleteNotificationChannel(alarm.id.toString(), context)
                iter.remove()
                break
            }
        }
    }

    private fun getById(id: Int): Alarm? {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                return alarm
            }
        }
        return null
    }

    private fun undoDestroy(context: Context) {
        if (canUndo) {
            addAlarm(lastDeleted.copy(), context)
            canUndo = false
        }
    }

    private fun edit(id: Int, hour: Int, minute: Int, context: Context) {
        val iter = alarms.iterator()
        val timeText = String.format("%02d:%02d", hour, minute)
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.hour = hour
                alarm.minute = minute
                alarm.timeText = timeText
                if(alarm.enable){
                    setAlarm(alarm, context)
                }
                break
            }
        }
    }

    private fun switchEnable(id: Int, enable: Boolean, context: Context) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.enable = enable
                if (enable) {
                    setAlarm(alarm, context)
                    createNotificationChannel(alarm.id.toString(), context)
                } else {
                    cancelAlarm(alarm, context)
                    deleteNotificationChannel(alarm.id.toString(), context)
                }
                break
            }
        }
    }

    private fun switchDetail(id: Int, isShowDetail: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.isShowDetail = isShowDetail
                break
            }
        }
    }

    private fun switchVibration(id: Int, isVibration: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.isVibration = isVibration
                break
            }
        }
    }

    private fun switchRepeatable(id: Int, isReadable: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.isRepeatable = isReadable
                break
            }
        }
    }

    private fun switchWeekEnable(id: Int, dayOfWeek: Int, dayEnable: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                when (dayOfWeek) {
                    Calendar.SUNDAY -> {
                        alarm.isSundayAlarm = dayEnable
                    }
                    Calendar.MONDAY -> {
                        alarm.isMondayAlarm = dayEnable
                    }
                    Calendar.TUESDAY -> {
                        alarm.isTuesdayAlarm = dayEnable
                    }
                    Calendar.WEDNESDAY -> {
                        alarm.isWednesdayAlarm = dayEnable
                    }
                    Calendar.THURSDAY -> {
                        alarm.isThursdayAlarm = dayEnable
                    }
                    Calendar.FRIDAY -> {
                        alarm.isFridayAlarm = dayEnable
                    }
                    Calendar.SATURDAY -> {
                        alarm.isSaturdayAlarm = dayEnable
                    }
                }
                if(!alarm.isSundayAlarm && !alarm.isMondayAlarm && !alarm.isTuesdayAlarm && !alarm.isWednesdayAlarm && !alarm.isThursdayAlarm && !alarm.isFridayAlarm && !alarm.isSaturdayAlarm){
                    alarm.isRepeatable = false
                    when (dayOfWeek) {
                        Calendar.SUNDAY -> {
                            alarm.isSundayAlarm = true
                        }
                        Calendar.MONDAY -> {
                            alarm.isMondayAlarm = true
                        }
                        Calendar.TUESDAY -> {
                            alarm.isTuesdayAlarm = true
                        }
                        Calendar.WEDNESDAY -> {
                            alarm.isWednesdayAlarm = true
                        }
                        Calendar.THURSDAY -> {
                            alarm.isThursdayAlarm = true
                        }
                        Calendar.FRIDAY -> {
                            alarm.isFridayAlarm = true
                        }
                        Calendar.SATURDAY -> {
                            alarm.isSaturdayAlarm = true
                        }
                    }
                }
                break
            }
        }
    }

    private fun selectSound(id: Int, soundFileName: String) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.soundFileName = soundFileName
                break
            }
        }
    }

    private fun addAlarm(alarm: Alarm, context: Context) {
        alarms.add(alarm)
        setAlarm(alarm, context)
        createNotificationChannel(alarm.id.toString(), context)
    }

    private fun createNotificationChannel(channelId: String, context: Context){
        // Create the NotificationChannel
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val alarmChannel = NotificationChannel(channelId, name, importance)
        alarmChannel.description = descriptionText
        alarmChannel.setSound(null, null)
        alarmChannel.group = context.getString(R.string.group_id)
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(alarmChannel)
    }

    private fun deleteNotificationChannel(channelId: String, context: Context){
        val notificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(channelId)
    }

    private fun setAlarm(alarm: Alarm, context: Context) {
        val setTime = LocalTime.of(alarm.hour, alarm.minute)
        val nowTime = LocalTime.of(LocalTime.now().hour, LocalTime.now().minute)
        val intent = Intent(context, AlarmBroadcastReceiver()::class.java)
        intent.putExtra("id", alarm.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmTime =
            if (setTime.isAfter(nowTime)) {
                LocalDateTime.of(LocalDate.now(), setTime)
            } else {
                LocalDateTime.of(LocalDate.now(), setTime).plusDays(1)
            }
        val alarmTimeMilli = alarmTime.toEpochSecond(OffsetDateTime.now().offset) * 1000 // seconds -> milliseconds
        val clockInfo = AlarmManager.AlarmClockInfo(alarmTimeMilli, null)
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.setAlarmClock(
                clockInfo,
                pendingIntent
            )
            val formatter = DateTimeFormatter.ofPattern("HH:mm にアラームをセットしました")
            val alarmText = alarmTime.format(formatter)
            Toast.makeText(context, alarmText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelAlarm(alarm: Alarm, context: Context){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver()::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id, intent, PendingIntent.FLAG_NO_CREATE
        )
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Toast.makeText(context, "このアラームを停止しました", Toast.LENGTH_SHORT).show()
        }
    }

    override fun createEvent(): StoreCreateEvent {
        return AlarmStoreCreateEvent()
    }

    override fun timeChangeEvent(): StoreTimeChangeEvent {
        return AlarmStoreTimeChangeEvent()
    }

    override fun changeEvent(): StoreChangeEvent {
        return AlarmStoreChangeEvent()
    }

    override fun destroyEvent(): StoreDestroyEvent {
        return AlarmStoreDestroyEvent()
    }

    override fun soundSelectEvent(): StoreSoundSelectEvent {
        return AlarmSoundSelectEvent()
    }

    class AlarmStoreCreateEvent : StoreCreateEvent
    class AlarmStoreTimeChangeEvent : StoreTimeChangeEvent
    class AlarmStoreChangeEvent : StoreChangeEvent
    class AlarmStoreDestroyEvent : StoreDestroyEvent
    class AlarmSoundSelectEvent : StoreSoundSelectEvent
}
