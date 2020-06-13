package net.banatech.app.android.sabi_alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import net.banatech.app.android.sabi_alarm.alarm.stores.AlarmStore

class AlarmBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val iter = AlarmStore.alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                Toast.makeText(context, "アラーム!", Toast.LENGTH_LONG).show()
                assetSoundStart(context, alarm.soundFileName)
                break
            }
        }
    }

    private fun assetSoundStart(context: Context, fileName:String){
        val assetFileDescriptor = context.assets.openFd("default/$fileName")
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(assetFileDescriptor)
        mediaPlayer.prepare()
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            it.stop()
            it.reset()
            it.release()
        }
    }
}
