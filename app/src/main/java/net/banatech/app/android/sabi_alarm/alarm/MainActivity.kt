package net.banatech.app.android.sabi_alarm.alarm

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alarm_view.view.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.database.Alarm
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private var timeDataset: ArrayList<Alarm> = arrayListOf()
    private var viewManager = LinearLayoutManager(this)
    private var viewAdapter =
        AlarmAdapter(timeDataset)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        add_alarm_button.setOnClickListener {
            val calendar = Calendar.getInstance()
            val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
            val nowMinute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _: TimePicker, pickerHour: Int, pickerMinute: Int ->
                    setAlarm(pickerHour, pickerMinute)
                },
                nowHour, nowMinute, true
            )
            timePickerDialog.show()
        }

        alarm_list.layoutManager = viewManager
        alarm_list.adapter = viewAdapter
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        alarm_list.addItemDecoration(dividerItemDecoration)
        alarm_list.setHasFixedSize(true)
        viewAdapter.setOnItemClickListener(object : AlarmAdapter.OnItemClickListener {
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
        Log.d("alarm id", alarmData.id.toString())
        timeDataset.add(alarmData)
        viewAdapter.notifyItemInserted(timeDataset.size - 1)
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
