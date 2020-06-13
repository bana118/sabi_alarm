package net.banatech.app.android.sabi_alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //TODO use intent filter for re-boot, date change ...
        Log.d("action", intent.action)
        val id = intent.getIntExtra("id", 0)
        val startActivityIntent = Intent(context, PlaySoundActivity::class.java)
        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityIntent.putExtra("id", id)
        context.startActivity(startActivityIntent)
    }

}
