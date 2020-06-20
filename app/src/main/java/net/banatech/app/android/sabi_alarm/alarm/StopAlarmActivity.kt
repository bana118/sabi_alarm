package net.banatech.app.android.sabi_alarm.alarm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_play_sound.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.actions.ActionsCreator
import net.banatech.app.android.sabi_alarm.alarm.stores.AlarmStore
import net.banatech.app.android.sabi_alarm.database.Alarm


class StopAlarmActivity : AppCompatActivity() {
    lateinit var alarm: Alarm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_play_sound)
        val id = intent.getIntExtra("id", 0)
        sound_stop_button.setOnClickListener {
            val alarm = AlarmStore.alarms.first { it.id == id }
            alarm.enable = false
            stopService(Intent(this, AlarmSoundService::class.java))
        }
    }
}
