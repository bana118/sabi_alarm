package net.banatech.app.android.sabi_alarm.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import java.io.IOException

class AlarmSoundService : Service(), MediaPlayer.OnCompletionListener {
    lateinit var alarm: Alarm
    lateinit var mediaPlayer: MediaPlayer
    private var vibrator: Vibrator? = null
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
        if (alarm.isDefaultSound) {
            val fileName = "default/${alarm.soundFileName}"
            val assetFileDescriptor = this.assets.openFd(fileName)
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(assetFileDescriptor)
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer.prepare()
                mediaPlayer.seekTo(alarm.soundStartTime)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val enableLoop = sharedPreferences.getBoolean("enable_sound_loop", false)
                    if (enableLoop) {
                        it.seekTo(alarm.soundStartTime)
                        it.start()
                    } else {
                        it.release()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            val fileUri = Uri.parse(alarm.soundFileUri)
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(this, fileUri)
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer.prepare()
                mediaPlayer.seekTo(alarm.soundStartTime)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val enableLoop = sharedPreferences.getBoolean("enable_sound_loop", false)
                    if (enableLoop) {
                        it.seekTo(alarm.soundStartTime)
                        it.start()
                    } else {
                        it.release()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stop() {
        vibrator?.cancel()
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}
