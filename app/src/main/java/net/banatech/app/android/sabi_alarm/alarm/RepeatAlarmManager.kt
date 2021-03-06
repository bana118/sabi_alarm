package net.banatech.app.android.sabi_alarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
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
        val alarmActivityIntent = Intent(context, AlarmActivity::class.java)
        val alarmActivityPendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                alarmActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val clockInfo = AlarmManager.AlarmClockInfo(alarmTimeMilli, alarmActivityPendingIntent)
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.setAlarmClock(
                clockInfo,
                pendingIntent
            )
            if (enableToast) {
                val toastLabel = context.getString(R.string.set_alarm_toast_label)
                val alarmText = "$toastLabel ${alarm.timeText}"
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
        val nowTimeMilli = LocalDateTime.now().toEpochSecond(OffsetDateTime.now().offset) * 1000
        val nextAlarmTimeMilli = calcDayOfWeekDiff(alarm, true) + nowTimeMilli
        val alarmActivityIntent = Intent(context, AlarmActivity::class.java)
        val alarmActivityPendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                alarmActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val clockInfo = AlarmManager.AlarmClockInfo(nextAlarmTimeMilli, alarmActivityPendingIntent)
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
                Toast.makeText(context, R.string.stop_alarm_toast_label, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calcDayOfWeekDiff(alarm: Alarm, isNextSet: Boolean): Long {
        val calender = Calendar.getInstance()
        // Sunday -> 0, Monday -> 1, ...
        val nowDayOfWeek = calender.get(Calendar.DAY_OF_WEEK) - 1
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
        if (isNextSet) {
            for (i in 1 until 8) {
                val index = (i + nowDayOfWeek) % 7
                if (weekList[index]) {
                    day = if (i == 0) 7 else i
                    break
                }
            }
        } else {
            for (i in 0 until 7) {
                val index = (i + nowDayOfWeek) % 7
                if (weekList[index]) {
                    day = i
                    break
                }
            }
        }
        if (day == 0 && isNextSet) {
            day = 7
        }
        return day.toLong() * 24 * 60 * 60 * 1000
    }
}
