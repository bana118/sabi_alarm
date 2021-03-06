package net.banatech.app.android.sabi_alarm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alarm_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.actions.alarm.AlarmActionsCreator
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import net.banatech.app.android.sabi_alarm.alarm.database.AlarmDatabase
import net.banatech.app.android.sabi_alarm.dispatcher.Dispatcher
import net.banatech.app.android.sabi_alarm.setting.SettingActivity
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore
import org.greenrobot.eventbus.Subscribe
import java.util.*
import com.google.android.gms.ads.MobileAds
import net.banatech.app.android.sabi_alarm.BuildConfig


class AlarmActivity : AppCompatActivity() {

    private lateinit var dispatcher: Dispatcher
    private lateinit var actionCreator: AlarmActionsCreator
    private lateinit var alarmStore: AlarmStore
    private lateinit var listAdapter: AlarmRecyclerAdapter

    companion object {
        lateinit var db: AlarmDatabase
    }

    private var viewManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AlarmDatabase.getInstance(applicationContext)
        val dao = db.alarmDao()
        if (AlarmStore.alarms.isEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main) {
                    AlarmStore.restoreAlarms(dao.getAll())
                    updateUI()
                    listAdapter.notifyDataSetChanged()
                }
            }
        }

        val channelId = getString(R.string.channel_id)
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val alarmChannel = NotificationChannel(channelId, name, importance)
        alarmChannel.description = descriptionText
        alarmChannel.setSound(null, null)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(alarmChannel)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initDependencies()
        setupView()

        MobileAds.initialize(this) {}
    }

    private fun initDependencies() {
        dispatcher = Dispatcher
        actionCreator =
            AlarmActionsCreator
        alarmStore = AlarmStore
    }

    private fun setupView() {
        add_alarm_button.setOnClickListener {
            val calendar = Calendar.getInstance()
            val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
            val nowMinute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _: TimePicker, pickerHour: Int, pickerMinute: Int ->
                    addAlarm(pickerHour, pickerMinute)
                },
                nowHour, nowMinute, true
            )
            timePickerDialog.show()
        }

        alarm_list.layoutManager = viewManager
        listAdapter = AlarmRecyclerAdapter(actionCreator)
        alarm_list.adapter = listAdapter
        updateUI()
        listAdapter.notifyDataSetChanged()
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        alarm_list.addItemDecoration(dividerItemDecoration)
        alarm_list.setHasFixedSize(true)
        listAdapter.setOnItemClickListener(object : AlarmRecyclerAdapter.OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int, alarm: Alarm) {
                actionCreator.switchDetail(alarm.id, !alarm.isShowDetail, applicationContext)
                val alarmDetail = view.include_alarm_detail
                if (alarm.isShowDetail) {
                    alarmDetail.visibility = View.VISIBLE
                    view.alarm_down_arrow.visibility = View.GONE
                } else {
                    alarmDetail.visibility = View.GONE
                    view.alarm_down_arrow.visibility = View.VISIBLE
                }
            }
        })
        listAdapter.setOnSoundStartTimeTextClickListener(object :
            AlarmRecyclerAdapter.OnSoundStartTimeTextClickListener {
            override fun onSoundStartTimeTextClickListener(
                view: View,
                position: Int,
                alarm: Alarm
            ) {
                val dialog = NumberPickerDialog()
                val soundStartTimeMillis = alarm.soundStartTime
                val sumSeconds = soundStartTimeMillis / 1000
                val initMinutes = sumSeconds / 60
                val initSeconds = sumSeconds % 60
                val initMillis = soundStartTimeMillis % 1000
                dialog.setDialogInit(
                    position,
                    alarm,
                    initMinutes,
                    initSeconds,
                    initMillis,
                    view.context,
                    listAdapter
                )
                dialog.show(supportFragmentManager, "再生開始時間")
            }
        })
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val alarmSortPreferenceValue = sharedPreferences.getString("alarm_sort", "0")?.toInt() ?: 0
        listAdapter.setItems(AlarmStore.alarms, alarmSortPreferenceValue)
    }

    private fun notifyDestroy() {
        if (AlarmStore.canUndo) {
            val snackbar = Snackbar.make(main_layout, R.string.delete_alarm_snackbar_label, Snackbar.LENGTH_LONG)
            snackbar.setAction(R.string.undo_delete_alarm_snackbar_label) {
                actionCreator.undoDestroy(this)
                listAdapter.notifyDataSetChanged()
            }
            snackbar.show()
        }
    }

    override fun onResume() {
        super.onResume()
        dispatcher.register(this)
        dispatcher.register(alarmStore)
        listAdapter.notifyDataSetChanged()

        if (alarm_ad_view_layout.childCount == 0) {
            val adView = AdView(this)
            adView.adSize = AdSize.BANNER
            adView.adUnitId = BuildConfig.ADMOB_BANNER_ID
            alarm_ad_view_layout.addView(adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }

        // これがないとアラーム起動時にAlarmStopActivityではなくAlarmActivityが起動する
        if (AlarmSoundService.mediaPlayer != null) {
            val stopSoundActivityIntent = Intent(this, AlarmStopActivity::class.java)
            startActivity(stopSoundActivityIntent)
        }
    }

    override fun onPause() {
        super.onPause()
        dispatcher.unregister(this)
        dispatcher.unregister(alarmStore)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, AlarmSoundService::class.java))
    }

    private fun addAlarm(hour: Int, minute: Int) {
        actionCreator.create(hour, minute, applicationContext)
        listAdapter.notifyDataSetChanged()
    }

    @Subscribe
    fun onAlarmStoreCreate(event: AlarmStore.AlarmStoreCreateEvent) {
        Log.d("event", "alarm create event")
        updateUI()
    }

    @Subscribe
    fun onAlarmStoreTimeChange(event: AlarmStore.AlarmStoreTimeChangeEvent) {
        Log.d("event", "alarm time change event")
        updateUI()
    }

    @Subscribe
    fun onAlarmStoreChange(event: AlarmStore.AlarmStoreChangeEvent) {
        Log.d("event", "alarm change event")
        updateUI()
    }

    @Subscribe
    fun onAlarmStoreDestroyEvent(event: AlarmStore.AlarmStoreDestroyEvent) {
        Log.d("event", "alarm destroy event")
        updateUI()
        notifyDestroy()
    }

    @Subscribe
    fun onAlarmSoundSelectEvent(event: AlarmStore.AlarmSoundSelectEvent) {
        Log.d("event", "alarm sound select event 1")
    }
}
