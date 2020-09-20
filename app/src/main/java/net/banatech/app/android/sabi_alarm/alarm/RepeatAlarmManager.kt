package net.banatech.app.android.sabi_alarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object RepeatAlarmManager {
    fun setAlarm(id: Int, context: Context, enableToast: Boolean) {
        val alarm = AlarmStore.alarms.first { it.id == id }
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
        val alarmTimeMilli = if (alarm.isRepeatable) {
            val nextAlarmDayMilli = calcDayOfWeekDiff(alarm, false)
            nextAlarmDayMilli + alarmTime.toEpochSecond(OffsetDateTime.now().offset) * 1000 // seconds -> milliseconds
        } else {
            alarmTime.toEpochSecond(OffsetDateTime.now().offset) * 1000 // seconds -> milliseconds
        }
        val clockInfo = AlarmManager.AlarmClockInfo(alarmTimeMilli, null)
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.setAlarmClock(
                clockInfo,
                pendingIntent
            )
            if (enableToast) {
                val formatter = DateTimeFormatter.ofPattern("HH:mm にアラームをセットしました")
                val alarmText = alarmTime.format(formatter)
                Toast.makeText(context, alarmText, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun nextSetAlarm(id: Int, context: Context) {
        val alarm = AlarmStore.alarms.first { it.id == id }
        val intent = Intent(context, AlarmBroadcastReceiver()::class.java)
        intent.putExtra("id", alarm.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nextAlarmTimeMilli = calcDayOfWeekDiff(alarm, true) + System.currentTimeMillis()
        val clockInfo = AlarmManager.AlarmClockInfo(nextAlarmTimeMilli, null)
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.setAlarmClock(
                clockInfo,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(id: Int, context: Context, enableToast: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver()::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_NO_CREATE
        )
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            if (enableToast) {
                Toast.makeText(context, "このアラームを停止しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calcDayOfWeekDiff(alarm: Alarm, isNextSet: Boolean): Long {
        val nowDayOfWeek = Calendar.DAY_OF_WEEK
        val weekList = listOf(
            alarm.isSundayAlarm,
            alarm.isMondayAlarm,
            alarm.isTuesdayAlarm,
            alarm.isWednesdayAlarm,
            alarm.isThursdayAlarm,
            alarm.isFridayAlarm,
            alarm.isSaturdayAlarm
        )
        var day = 0
        for (i in nowDayOfWeek until nowDayOfWeek + 7) {
            val index = i % 7
            if (weekList[index]) {
                day = i % 7
                break
            }
        }
        if (day == 0 && isNextSet) {
            day = 7
        }
        return day.toLong() * 24 * 60 * 60 * 1000

    }
}
