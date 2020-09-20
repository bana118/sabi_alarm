package net.banatech.app.android.sabi_alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.banatech.app.android.sabi_alarm.alarm.database.AlarmDatabase
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore


class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //TODO use intent filter for re-boot, date change ...
        val id = intent.getIntExtra("id", 0)
        val startServiceIntent = Intent(context, AlarmSoundService::class.java)
        if(AlarmStore.alarms.isEmpty()) {
            val db = AlarmDatabase.getInstance(context)
            val dao = db.alarmDao()
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main){
                    AlarmStore.restoreAlarms(dao.getAll())
                    val alarm = AlarmStore.alarms.first{it.id == id}
                    if(alarm.isRepeatable){
                        RepeatAlarmManager.nextSetAlarm(id, context)
                    }
                    startServiceIntent.putExtra("id", id)
                    context.startForegroundService(startServiceIntent)
                }
            }
        } else {
            val alarm = AlarmStore.alarms.first{it.id == id}
            if(alarm.isRepeatable){
                RepeatAlarmManager.nextSetAlarm(id, context)
            }
            startServiceIntent.putExtra("id", id)
            context.startForegroundService(startServiceIntent)
        }
    }
}
