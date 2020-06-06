package net.banatech.app.android.sabi_alarm.alarm

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alarm_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.actions.ActionsCreator
import net.banatech.app.android.sabi_alarm.alarm.dispatcher.Dispatcher
import net.banatech.app.android.sabi_alarm.alarm.stores.AlarmStore
import net.banatech.app.android.sabi_alarm.database.Alarm
import net.banatech.app.android.sabi_alarm.database.AlarmDatabase
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.math.min


class AlarmActivity : AppCompatActivity() {

    private lateinit var dispatcher: Dispatcher
    private lateinit var actionCreator: ActionsCreator
    private lateinit var alarmStore: AlarmStore
    private lateinit var listAdapter: AlarmRecyclerAdapter


    companion object {
        lateinit var db: AlarmDatabase
    }
    private var timeDataset: ArrayList<Alarm> = arrayListOf()
    private var viewManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initDependencies()
        setupView()
//        db = AlarmDatabase.getInstance(this.applicationContext)
//        val dao = db.alarmDao()
//        CoroutineScope(Dispatchers.Main).launch {
//            withContext(Dispatchers.Main){
//                dao.getAll().forEach{
//                    timeDataset.add(it)
//                }
//                listAdapter.notifyDataSetChanged()
//            }
//        }

    }

    private fun initDependencies() {
        dispatcher = Dispatcher
        actionCreator = ActionsCreator(dispatcher)
        alarmStore = AlarmStore(dispatcher)
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
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        alarm_list.addItemDecoration(dividerItemDecoration)
        alarm_list.setHasFixedSize(true)
        listAdapter.setOnItemClickListener(object : AlarmRecyclerAdapter.OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                val alarmDetail = view.include_alarm_detail
                val downArrow = view.alarm_down_arrow
                check(alarmDetail.visibility == View.GONE || alarmDetail.visibility == View.VISIBLE)
                {
                    "Alarm detail layout visibility is invalid"
                }
                if (alarmDetail.visibility == View.GONE) {
                    alarmDetail.visibility = View.VISIBLE
                    downArrow.visibility = View.GONE
                } else {
                    alarmDetail.visibility = View.GONE
                    downArrow.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setAlarm(hour: Int, minute: Int) {
        val setTimeText = String.format("%02d:%02d", hour, minute)
        val alarmData = Alarm(
            hour = hour,
            minute = minute,
            timeText = setTimeText,
            isVibration = false,
            isRepeatable = false,
            isSundayAlarm = false,
            isMondayAlarm = true,
            isTuesdayAlarm = false,
            isWednesdayAlarm = true,
            isThursdayAlarm = true,
            isFridayAlarm = false,
            isSaturdayAlarm = true,
            soundFileName = "beethoven_no5_1st.mp3",
            soundStartTime = 0,
            isDefaultSound = true
        )
        val dao = db.alarmDao()
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default){
                dao.insertAll(alarmData)
                Log.d("debug", dao.getAll().toString())
            }
        }
        timeDataset.add(alarmData)
        listAdapter.notifyItemInserted(timeDataset.size - 1)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI() {
        listAdapter.setItems(alarmStore.alarms)

        if(alarmStore.canUndo) {
            val snackbar = Snackbar.make(main_layout, "削除しました", Snackbar.LENGTH_LONG)
            snackbar.setAction("Undo") {actionCreator.undoDestroy()}
            snackbar.show()
        }
    }

    override fun onResume() {
        super.onResume()
        dispatcher.register(this)
        dispatcher.register(alarmStore)
    }

    override fun onPause() {
        super.onPause()
        dispatcher.unregister(this)
        dispatcher.unregister(alarmStore)
    }

    private fun addAlarm(hour: Int, minute: Int){
        actionCreator.create(hour, minute)
    }

    @Subscribe
    fun onAlarmStoreChange(event: AlarmStore.AlarmStoreChangeEvent) {
        Log.d("debug", "subscribe event")
        updateUI()
    }
}
