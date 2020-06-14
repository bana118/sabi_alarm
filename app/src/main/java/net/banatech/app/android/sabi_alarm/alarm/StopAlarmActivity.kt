package net.banatech.app.android.sabi_alarm.alarm

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_play_sound.*
import net.banatech.app.android.sabi_alarm.R


class StopAlarmActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_play_sound)
        val id = this.intent.getIntExtra("id", 0)
        //val startIntent = Intent(this, PlaySoundService::class.java)
        //startIntent.putExtra("id", id)
        //startService(startIntent)

        sound_stop_button.setOnClickListener{
            stopService(Intent(this, AlarmSoundService::class.java))
        }
    }
}
