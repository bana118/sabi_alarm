package net.banatech.app.android.sabi_alarm.alarm

import android.app.TimePickerDialog
import android.content.Intent
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_detail.view.*
import kotlinx.android.synthetic.main.alarm_view.view.*
import kotlinx.android.synthetic.main.alarm_week.view.*
import kotlinx.coroutines.processNextEventInCurrentThread
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.actions.ActionsCreator
import net.banatech.app.android.sabi_alarm.database.Alarm
import net.banatech.app.android.sabi_alarm.sound.SoundSelectActivity
import java.util.*
import kotlin.collections.ArrayList

class AlarmRecyclerAdapter(actionsCreator: ActionsCreator) :
    RecyclerView.Adapter<AlarmRecyclerAdapter.AlarmViewHolder>() {
    lateinit var listener: OnItemClickListener

    companion object {
        lateinit var actionsCreator: ActionsCreator
    }

    private var alarms: ArrayList<Alarm> = ArrayList()

    init {
        AlarmRecyclerAdapter.actionsCreator = actionsCreator
    }

    class AlarmViewHolder(val alarmView: View) : RecyclerView.ViewHolder(alarmView)

    override fun getItemId(position: Int): Long {
        setHasStableIds(true)
        return alarms[position].id.toLong()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlarmViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_view, parent, false)
        return AlarmViewHolder(
            textView
        )
    }

    override fun onBindViewHolder(viewHolder: AlarmViewHolder, position: Int) {
        //viewHolder.bindView(alarms[position])
        //val dao = AlarmActivity.db.alarmDao()

        viewHolder.alarmView.alarm_time.text = alarms[position].timeText
        viewHolder.alarmView.setOnClickListener {
            listener.onItemClickListener(it, position, alarms[position])
        }

        //Edit alarm time
        viewHolder.alarmView.alarm_time.setOnClickListener {
            val hour = alarms[position].hour
            val minute = alarms[position].minute
            val timePickerDialog = TimePickerDialog(
                viewHolder.alarmView.context,
                TimePickerDialog.OnTimeSetListener { _: TimePicker, pickerHour: Int, pickerMinute: Int ->
                    actionsCreator.edit(alarms[position].id, pickerHour, pickerMinute)
                    notifyDataSetChanged()
                },
                hour, minute, true
            )
            timePickerDialog.show()
        }

        val alarmSwitch = viewHolder.alarmView.alarm_switch

        //Switch alarm on/off
        //alarmSwitch.isChecked = alarms[position].enable
        alarmSwitch.setOnClickListener {
            actionsCreator.switchEnable(alarms[position].id, alarmSwitch.isChecked)
            //notifyItemChanged(position)
        }

        val alarmDetail = viewHolder.alarmView.include_alarm_detail
        if (alarms[position].isShowDetail) {
            alarmDetail.visibility = View.VISIBLE
            viewHolder.alarmView.alarm_down_arrow.visibility = View.GONE
        } else {
            alarmDetail.visibility = View.GONE
            viewHolder.alarmView.alarm_down_arrow.visibility = View.VISIBLE
        }

        //Switch alarm vibration
        //alarmDetail.vibration_check_box.isChecked = alarms[position].isVibration
        alarmDetail.vibration_check_box.setOnClickListener {
            actionsCreator.switchVibration(
                alarms[position].id,
                alarmDetail.vibration_check_box.isChecked
            )
            //notifyItemChanged(position)
        }
        alarmDetail.vibration_check_box.isChecked = alarms[position].isVibration

        //Switch alarm repeatable
        //alarmDetail.repeat_check_box.isChecked = alarms[position].isRepeatable
        alarmDetail.repeat_check_box.setOnClickListener {
            actionsCreator.switchRepeatable(
                alarms[position].id,
                alarmDetail.repeat_check_box.isChecked
            )
            if (alarms[position].isRepeatable) {
                alarmDetail.include_alarm_week.visibility = View.VISIBLE
            } else {
                alarmDetail.include_alarm_week.visibility = View.GONE
            }
            //notifyItemChanged(position)
        }
        alarmDetail.repeat_check_box.isChecked = alarms[position].isRepeatable
        if (alarms[position].isRepeatable) {
            alarmDetail.include_alarm_week.visibility = View.VISIBLE
        } else {
            alarmDetail.include_alarm_week.visibility = View.GONE
        }

        //Switch day of the day alarm
        val week = alarmDetail.include_alarm_week
        val weekList = listOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        )
        val weekAlarmArray = arrayOf(
            alarms[position].isSundayAlarm,
            alarms[position].isMondayAlarm,
            alarms[position].isTuesdayAlarm,
            alarms[position].isWednesdayAlarm,
            alarms[position].isThursdayAlarm,
            alarms[position].isFridayAlarm,
            alarms[position].isSaturdayAlarm
        )
        val weekButtonList = listOf(
            week.sunday_button,
            week.monday_button,
            week.tuesday_button,
            week.wednesday_button,
            week.thursday_button,
            week.friday_button,
            week.saturday_button
        )
        for(i in 0 until 7){
            weekButtonList[i].setOnClickListener{
                actionsCreator.switchDayAlarm(
                    alarms[position].id,
                    weekList[i],
                    !weekAlarmArray[i]
                )
                weekAlarmArray[i] = !weekAlarmArray[i]
                if(weekAlarmArray[i]) {
                    selectWeekButton(weekButtonList[i], viewHolder)
                }else{
                    unselectWeekButton(weekButtonList[i], viewHolder)
                }
            }
            if(weekAlarmArray[i]) {
                selectWeekButton(weekButtonList[i], viewHolder)
            }else{
                unselectWeekButton(weekButtonList[i], viewHolder)
            }
        }

        //Destroy alarm
        alarmDetail.delete_button.setOnClickListener {
            alarmDetail.visibility = View.GONE
            viewHolder.alarmView.alarm_down_arrow.visibility = View.VISIBLE
            alarmDetail.repeat_check_box.isChecked = false
            alarmDetail.include_alarm_week.visibility = View.GONE
            alarmDetail.vibration_check_box.isChecked = false
            alarmSwitch.isChecked = true
            for(i in 0 until 7){
                if(i == 0 || i == 6){
                    unselectWeekButton(weekButtonList[i], viewHolder)
                }else{
                    selectWeekButton(weekButtonList[i], viewHolder)
                }
            }
            actionsCreator.destroy(alarms[position].id)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, alarms.size)
        }

        //Select alarm sound
        alarmDetail.sound_button.setOnClickListener {
            val intent = Intent(alarmDetail.context, SoundSelectActivity::class.java)
            alarmDetail.context.startActivity(intent)
        }
    }

    private fun selectWeekButton(weekButton: Button, viewHolder: AlarmViewHolder) {
        weekButton.setTextColor(
            ContextCompat.getColor(
                viewHolder.alarmView.context,
                R.color.week_text_selected_color
            )
        )
        weekButton.setBackgroundResource(R.drawable.selected_round_button)
    }

    private fun unselectWeekButton(weekButton: Button, viewHolder: AlarmViewHolder) {
        weekButton.setTextColor(
            ContextCompat.getColor(
                viewHolder.alarmView.context,
                R.color.week_text_unselected_color
            )
        )
        weekButton.setBackgroundResource(R.drawable.unselected_round_button)
    }


    override fun getItemCount() = alarms.size

    fun setItems(alarms: ArrayList<Alarm>) {
//        Log.d("size", alarms.size.toString())
//        for(alarm in alarms) {
//            Log.d("id", alarm.id.toString())
//            Log.d("isVibration", alarm.isVibration.toString())
//            Log.d("isRepeatable", alarm.isRepeatable.toString())
//        }
        alarms.sortWith(compareBy({ it.hour }, { it.minute }))
        Log.d("current", this.alarms.toString())
        Log.d("next", alarms.toString())
        this.alarms = alarms
        //notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int, alarm: Alarm)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}
