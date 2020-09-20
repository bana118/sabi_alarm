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

class AlarmStartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val db = AlarmDatabase.getInstance(context)
            val dao = db.alarmDao()
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main) {
                    AlarmStore.rebootAlarms(dao.getAll(), context)
                }
            }
        }

    }
}
