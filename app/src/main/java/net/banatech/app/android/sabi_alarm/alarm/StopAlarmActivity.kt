package net.banatech.app.android.sabi_alarm.alarm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_play_sound.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore


class StopAlarmActivity : AppCompatActivity() {
    lateinit var alarm: Alarm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_play_sound)
        val id = intent.getIntExtra("id", 0)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val stopAlarmMinutes = sharedPreferences.getString("stop_sound_time", "0")?.toLong()
        if (stopAlarmMinutes != null && stopAlarmMinutes > 0) {
            Handler().postDelayed({
                val alarm = AlarmStore.alarms.first { it.id == id }
                if (!alarm.isRepeatable) {
                    alarm.enable = false
                    AlarmStore.updateDb(alarm)
                }
                stopService(Intent(this, AlarmSoundService::class.java))
                startActivity(Intent(this, AlarmActivity::class.java))
                finish()
            }, stopAlarmMinutes * 60 * 1000)
        }
        sound_stop_button.setOnClickListener {
            val alarm = AlarmStore.alarms.first { it.id == id }
            if (!alarm.isRepeatable) {
                alarm.enable = false
                AlarmStore.updateDb(alarm)
            }
            stopService(Intent(this, AlarmSoundService::class.java))
            startActivity(Intent(this, AlarmActivity::class.java))
            finish()
        }
    }
}
