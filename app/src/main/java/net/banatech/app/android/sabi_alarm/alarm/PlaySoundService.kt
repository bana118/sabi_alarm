package net.banatech.app.android.sabi_alarm.alarm

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import net.banatech.app.android.sabi_alarm.alarm.stores.AlarmStore
import net.banatech.app.android.sabi_alarm.database.Alarm
import java.io.IOException

class PlaySoundService : Service(), MediaPlayer.OnCompletionListener {
    lateinit var alarm: Alarm
    lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer = MediaPlayer()
        val iter = AlarmStore.alarms.iterator()
        check(intent != null)
        val id = intent.getIntExtra("id", 0)
        var isFound = false
        while (iter.hasNext()) {
            val iterAlarm = iter.next()
            if (iterAlarm.id == id) {
                isFound = true
                alarm = iterAlarm
                play()
                break
            }
        }
        if (!isFound) {
            Log.d("debug", "Alarm(id=$id) is not found")
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        play()
    }

    private fun play() {
        val fileName = if (alarm.isDefaultSound) {
            "default/${alarm.soundFileName}"
        } else {
            "default/${alarm.soundFileName}"
        }
        val assetFileDescriptor = this.assets.openFd(fileName)
        Log.d("debug", alarm.soundFileName)
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(assetFileDescriptor)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stop() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}

