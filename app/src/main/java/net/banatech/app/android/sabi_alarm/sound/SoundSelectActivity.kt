package net.banatech.app.android.sabi_alarm.sound

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.sound_select.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.actions.sound.SoundActionsCreator
import net.banatech.app.android.sabi_alarm.alarm.AlarmSoundService
import net.banatech.app.android.sabi_alarm.alarm.StopAlarmActivity
import net.banatech.app.android.sabi_alarm.dispatcher.Dispatcher
import net.banatech.app.android.sabi_alarm.setting.SettingActivity
import net.banatech.app.android.sabi_alarm.sound.database.SoundDatabase
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore
import net.banatech.app.android.sabi_alarm.stores.sound.SoundStore
import org.greenrobot.eventbus.Subscribe


class SoundSelectActivity : AppCompatActivity() {
    companion object {
        lateinit var db: SoundDatabase
    }

    private val readRequestCode = 42

    private lateinit var localSoundAdapter: LocalSoundRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = SoundDatabase.getInstance(this.applicationContext)
        val dao = db.soundDao()
        if (SoundStore.sounds.isEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main) {
                    SoundStore.restoreSounds(dao.getAll())
                    updateUI()
                    localSoundAdapter.notifyDataSetChanged()
                }
            }
        }
        if (AlarmSoundService.mediaPlayer != null) {
            val stopSoundActivityIntent = Intent(this, StopAlarmActivity::class.java)
            stopSoundActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(stopSoundActivityIntent)
        }
        setContentView(R.layout.sound_select)
        setSupportActionBar(toolbar)
        val selectedAlarmId = intent.getIntExtra("ALARM_ID", 0)
        val selectedAlarm = AlarmStore.alarms.first { it.id == selectedAlarmId }
        AlarmStore.selectedAlarm = selectedAlarm
        val assetManager = this.resources.assets
        val defaultSoundDir = assetManager.list("default")
        check(defaultSoundDir != null) { "default sound list must not be null" }
        val defaultSoundAdapter = DefaultSoundRecyclerAdapter(defaultSoundDir)
        localSoundAdapter = LocalSoundRecyclerAdapter(SoundActionsCreator, defaultSoundAdapter)
        defaultSoundAdapter.setLocalSoundAdapter(localSoundAdapter)
        local_sound_list.layoutManager = LinearLayoutManager(this)
        local_sound_list.adapter = localSoundAdapter
        updateUI()
        localSoundAdapter.notifyDataSetChanged()
        default_sound_list.layoutManager = LinearLayoutManager(this)
        default_sound_list.adapter = defaultSoundAdapter
        add_local_sound_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }
            startActivityForResult(intent, readRequestCode)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (!Dispatcher.isRegistered(this)) {
            Dispatcher.register(this)
        }
        if (!Dispatcher.isRegistered(SoundStore)) {
            Dispatcher.register(SoundStore)
        }
        if (!Dispatcher.isRegistered(AlarmStore)) {
            Dispatcher.register(AlarmStore)
        }
        if (requestCode == readRequestCode && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val fileName = getFileName(uri)
                check(fileName is String) { "FileName must be String" }
                this.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                SoundActionsCreator.add(fileName, uri.toString(), this)
                localSoundAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Dispatcher.isRegistered(this)) {
            Dispatcher.register(this)
        }
        if (!Dispatcher.isRegistered(SoundStore)) {
            Dispatcher.register(SoundStore)
        }
        if (!Dispatcher.isRegistered(AlarmStore)) {
            Dispatcher.register(AlarmStore)
        }
    }

    override fun onPause() {
        super.onPause()
        Dispatcher.unregister(this)
        Dispatcher.unregister(SoundStore)
        Dispatcher.unregister(AlarmStore)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val soundSortPreferenceValue = sharedPreferences.getString("sound_sort", "0")?.toInt() ?: 0
        localSoundAdapter.setItems(SoundStore.sounds, soundSortPreferenceValue)
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.getPath()
            val cut = result?.lastIndexOf('/')
            check(cut is Int)
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    @Subscribe
    fun onSoundAddEvent(event: SoundStore.SoundStoreAddEvent) {
        Log.d("event", "alarm sound add event")
        updateUI()
    }

    @Subscribe
    fun onSoundRemoveEvent(event: SoundStore.SoundStoreRemoveEvent) {
        Log.d("event", "alarm sound remove event")
        updateUI()
    }

    @Subscribe
    fun onSoundSelectEvent(event: AlarmStore.AlarmSoundSelectEvent) {
        Log.d("event", "alarm sound select event 2")
        updateUI()
    }
}
