package net.banatech.app.android.sabi_alarm

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alarm_detail.view.*
import kotlinx.android.synthetic.main.alarm_view.view.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private var timeDataset: ArrayList<Alarm> = arrayListOf()
    private var viewManager = LinearLayoutManager(this)
    private var viewAdapter = AlarmAdapter(timeDataset)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val calendar = Calendar.getInstance()
            val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
            val nowMinute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener{
                 _: TimePicker, pickerHour: Int, pickerMinute: Int ->
                    setAlarm(pickerHour, pickerMinute)
                },
                nowHour, nowMinute, true)
            timePickerDialog.show()
        }

        alarm_list.layoutManager = viewManager
        alarm_list.adapter = viewAdapter
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        alarm_list.addItemDecoration(dividerItemDecoration)
        alarm_list.setHasFixedSize(true)
        viewAdapter.setOnItemClickListener(object:AlarmAdapter.OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                val alarmDetail = view.include_alarm_detail
                if(alarmDetail.visibility == View.GONE){
                    alarmDetail.visibility = View.VISIBLE
                }else{
                    alarmDetail.visibility = View.GONE
                }
            }
        })

    }

    private fun setAlarm(hour: Int, minute: Int) {
        val df = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.JAPAN)
        val currentTime = df.format(Date()).toLong()
        val setTimeText = String.format("%02d:%02d", hour, minute)
        val weekAlarmList = (0 until 7).map{false}.toBooleanArray()
        val alarmData = Alarm(currentTime, setTimeText,
            isVibration = false,
            isRepeatable = false,
            weekAlarmList = weekAlarmList
        )
        timeDataset.add(alarmData)
        viewAdapter.notifyItemInserted(timeDataset.size-1)
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
}
