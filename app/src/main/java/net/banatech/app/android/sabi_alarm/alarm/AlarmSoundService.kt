package net.banatech.app.android.sabi_alarm.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.stores.AlarmStore
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import java.io.IOException

class AlarmSoundService : Service(), MediaPlayer.OnCompletionListener {
    lateinit var alarm: Alarm
    lateinit var mediaPlayer: MediaPlayer
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        require(intent != null)
        val id = intent.getIntExtra("id", 0)
        mediaPlayer = MediaPlayer()
        alarm = AlarmStore.alarms.first { it.id == id }
        val stopSoundActivityIntent = Intent(this, StopAlarmActivity::class.java)
        stopSoundActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        stopSoundActivityIntent.putExtra("id", id)
        val channelId = getString(R.string.channel_id)
        val stopSoundFullScreenIntent = Intent(stopSoundActivityIntent)
        val stopSoundFullScreenPendingIntent = PendingIntent.getActivity(
            this,
            id,
            stopSoundFullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(this.getString(R.string.app_name))
            .setContentText("アラーム！")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .addAction(0, "アラームを停止", stopSoundFullScreenPendingIntent)
            .setFullScreenIntent(stopSoundFullScreenPendingIntent, true)

        if (alarm.isVibration) {
            notificationBuilder.setVibrate(longArrayOf(0, 1000, 100))
        }

        val notification = notificationBuilder.build()
        startForeground(1, notification)
        play()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        play()
    }

    private fun play() {
        val fileName = if (alarm.isDefaultSound) {
            "default/${alarm.soundFileName}"
        } else {
            "default/${alarm.soundFileName}" // TODO impl not default
        }
        val assetFileDescriptor = this.assets.openFd(fileName)
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(assetFileDescriptor)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
            mediaPlayer.prepare()
            mediaPlayer.seekTo(alarm.soundStartTime)
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
