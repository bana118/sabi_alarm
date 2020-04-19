package net.banatech.app.android.sabi_alarm

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var timeDataset: ArrayList<String> = arrayListOf()
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
        alarm_list.setHasFixedSize(true)
        viewAdapter.setOnItemClickListener(object:AlarmAdapter.OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                Toast.makeText(applicationContext, "${clickedText}がタップされました", Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun setAlarm(hour: Int, minute: Int) {
        timeDataset.add("$hour:$minute")
        viewAdapter.notifyDataSetChanged()
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
