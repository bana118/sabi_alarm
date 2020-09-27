package net.banatech.app.android.sabi_alarm.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import net.banatech.app.android.sabi_alarm.sound.LocalSoundRecyclerAdapter
import java.io.IOException

class AlarmSoundService : Service(), MediaPlayer.OnCompletionListener {
    lateinit var alarm: Alarm

    companion object {
        var mediaPlayer: MediaPlayer? = null
        private var vibrator: Vibrator? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        require(intent != null)
        val id = intent.getIntExtra("id", 0)
        stop()
        Log.d("debug1", AlarmStore.alarms.toString())
        Log.d("debug2", id.toString())
        alarm = AlarmStore.alarms.first { it.id == id }
        val stopSoundActivityIntent = Intent(this, StopAlarmActivity::class.java)
        stopSoundActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val channelId = getString(R.string.channel_id)
        val stopSoundFullScreenIntent = Intent(stopSoundActivityIntent)
        val stopSoundFullScreenPendingIntent = PendingIntent.getActivity(
            this,
            id,
            stopSoundFullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.sabi_alarm_round)
            .setContentTitle(this.getString(R.string.app_name))
            .setContentText("アラーム！")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .addAction(0, "アラームを停止", stopSoundFullScreenPendingIntent)
            .setFullScreenIntent(stopSoundFullScreenPendingIntent, true)

        val notification = notificationBuilder.build()
        startForeground(1, notification)
        play()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val stopAlarmMinutes = sharedPreferences.getString("stop_sound_time", "0")?.toLong() ?: 0
        if (stopAlarmMinutes > 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                val alarm = AlarmStore.alarms.first { it.id == id }
                if (!alarm.isRepeatable) {
                    alarm.enable = false
                    AlarmStore.updateDb(alarm, this)
                }
                stopService(Intent(this, AlarmSoundService::class.java))

            }, stopAlarmMinutes * 60 * 1000)
        }
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
        mediaPlayer = MediaPlayer()
        val context = applicationContext
        if (alarm.isVibration) {
            vibrator =
                ContextCompat.getSystemService(context, Vibrator::class.java)
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(200, 500),
                intArrayOf(0, DEFAULT_AMPLITUDE),
                0
            )
            vibrator?.vibrate(vibrationEffect)
        }
        val isAvailable =
             alarm.isDefaultSound || LocalSoundRecyclerAdapter.isAvailable(Uri.parse(alarm.soundFileUri), context)
        if (alarm.isDefaultSound) {
            val fileName = "default/${alarm.soundFileName}"
            val assetFileDescriptor = this.assets.openFd(fileName)
            try {
                mediaPlayer?.setDataSource(assetFileDescriptor)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            if (isAvailable) {
                val fileUri = Uri.parse(alarm.soundFileUri)
                try {
                    mediaPlayer?.setDataSource(context, fileUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                val assetManager = context.resources.assets
                val defaultSoundDir = assetManager.list("default")
                check(defaultSoundDir != null) { "default sound list must not be null" }
                val fileName = "default/${defaultSoundDir.first()}"
                val assetFileDescriptor = this.assets.openFd(fileName)
                try {
                    mediaPlayer?.setDataSource(assetFileDescriptor)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mediaPlayer?.prepare()
        if (isAvailable) {
            mediaPlayer?.seekTo(alarm.soundStartTime)
        } else {
            mediaPlayer?.seekTo(0)
        }

        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val soundFinishAction =
                sharedPreferences.getString("sound_finish_action", "0")?.toInt() ?: 0
            when (soundFinishAction) {
                0 -> {
                    if (isAvailable) {
                        it.seekTo(alarm.soundStartTime)
                    } else {
                        it.seekTo(0)
                    }
                    it.start()
                }
                1 -> {
                    it.seekTo(0)
                    it.start()
                }
                2 -> {
                    it.release()
                    if (!alarm.isRepeatable) {
                        alarm.enable = false
                        AlarmStore.updateDb(alarm, this)
                    }
                    stopService(Intent(this, AlarmSoundService::class.java))
                }
            }

        }
    }

    private fun stop() {
        vibrator?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        vibrator = null
        mediaPlayer = null
    }
}
