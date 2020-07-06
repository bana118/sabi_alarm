package net.banatech.app.android.sabi_alarm.sound

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.sound_select.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.dispatcher.Dispatcher
import net.banatech.app.android.sabi_alarm.alarm.stores.AlarmStore
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import org.greenrobot.eventbus.Subscribe

class SoundSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sound_select)
        setSupportActionBar(toolbar)
        val selectedAlarmId = intent.getIntExtra("ALARM_ID", 0)
        var selectedAlarm: Alarm? = null
        for(alarm in AlarmStore.alarms) {
            if(alarm.id == selectedAlarmId){
                selectedAlarm = alarm
            }
        }
        check(selectedAlarm != null){ "SelectedAlarm must not be null" }
        AlarmStore.selectedAlarm = selectedAlarm
        val assetManager = this.resources.assets
        val defaultSoundDir = assetManager.list("default")
        check(defaultSoundDir != null) { "default sound list must not be null" }
        val defaultSoundAdapter = DefaultSoundRecyclerAdapter(defaultSoundDir)
        val localSoundAdapter = LocalSoundRecyclerAdapter(arrayOf("1","2","3","4","5","6","7","8","9","10","11"))
        local_sound_list.layoutManager = LinearLayoutManager(this)
        local_sound_list.adapter = localSoundAdapter
        default_sound_list.layoutManager =LinearLayoutManager(this)
        default_sound_list.adapter = defaultSoundAdapter
        add_local_sound_button.setOnClickListener {
            val READ_REQUEST_CODE = 42

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }

            startActivityForResult(intent, READ_REQUEST_CODE)
        }

    }

    override fun onResume() {
        super.onResume()
        Dispatcher.register(this)
        Dispatcher.register(AlarmStore)
    }

    override fun onPause() {
        super.onPause()
        Dispatcher.unregister(this)
        Dispatcher.unregister(AlarmStore)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Subscribe
    fun onAlarmSoundSelectyEvent(event: AlarmStore.AlarmSoundSelectEvent) {
        Log.d("event", "alarm sound select event 2")
    }
}