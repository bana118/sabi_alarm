package net.banatech.app.android.sabi_alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.banatech.app.android.sabi_alarm.alarm.stores.AlarmStore


class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //TODO use intent filter for re-boot, date change ...
        val id = intent.getIntExtra("id", 0)
        val startServiceIntent = Intent(context, AlarmSoundService::class.java)
        val alarm = AlarmStore.alarms.first{it.id == id}
        if(alarm.isRepeatable){
            RepeatAlarmManager.nextSetAlarm(id, context)
        }
        startServiceIntent.putExtra("id", id)
        context.startForegroundService(startServiceIntent)
    }
}
