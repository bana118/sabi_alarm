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
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.actions.ActionsCreator
import net.banatech.app.android.sabi_alarm.database.Alarm
import net.banatech.app.android.sabi_alarm.sound.SoundSelectActivity
import kotlin.collections.ArrayList

class AlarmRecyclerAdapter(actionsCreator: ActionsCreator) :
    RecyclerView.Adapter<AlarmRecyclerAdapter.AlarmViewHolder>() {
    lateinit var listener: OnItemClickListener
    companion object{
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
        alarmSwitch.setOnClickListener{
            actionsCreator.switchEnable(alarms[position].id, alarmSwitch.isChecked)
            //notifyItemChanged(position)
        }

        val alarmDetail = viewHolder.alarmView.include_alarm_detail
//        if(alarms[position].isShowDetail){
//            alarmDetail.visibility = View.VISIBLE
//            viewHolder.alarmView.alarm_down_arrow.visibility = View.GONE
//        }else{
//            alarmDetail.visibility = View.GONE
//            viewHolder.alarmView.alarm_down_arrow.visibility = View.VISIBLE
//        }

        //Switch alarm vibration
        //alarmDetail.vibration_check_box.isChecked = alarms[position].isVibration
        alarmDetail.vibration_check_box.setOnClickListener{
            actionsCreator.switchVibration(alarms[position].id, alarmDetail.vibration_check_box.isChecked)
            //notifyItemChanged(position)
        }

        //Switch alarm repeatable
        //alarmDetail.repeat_check_box.isChecked = alarms[position].isRepeatable
        alarmDetail.repeat_check_box.setOnClickListener{
            actionsCreator.switchRepeatable(alarms[position].id, alarmDetail.repeat_check_box.isChecked)
            if(alarms[position].isRepeatable) {
                alarmDetail.include_alarm_week.visibility = View.VISIBLE
            }else{
                alarmDetail.include_alarm_week.visibility = View.GONE
            }
            //notifyItemChanged(position)
        }
//        if(alarms[position].isRepeatable) {
//            alarmDetail.include_alarm_week.visibility = View.VISIBLE
//        }else{
//            alarmDetail.include_alarm_week.visibility = View.GONE
//        }

        val weekdayButtons = listOf(
            alarmDetail.include_alarm_week.monday_button,
            alarmDetail.include_alarm_week.tuesday_button,
            alarmDetail.include_alarm_week.wednesday_button,
            alarmDetail.include_alarm_week.thursday_button,
            alarmDetail.include_alarm_week.friday_button
        )
        val weekendButtons = listOf(
            alarmDetail.include_alarm_week.sunday_button,
            alarmDetail.include_alarm_week.saturday_button
        )

        //Destroy alarm
        alarmDetail.delete_button.setOnClickListener {
            alarmDetail.visibility = View.GONE
            viewHolder.alarmView.alarm_down_arrow.visibility = View.VISIBLE
            actionsCreator.destroy(alarms[position].id)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, alarms.size)
            //alarmDetail.visibility = View.GONE
            //viewHolder.alarmView.alarm_down_arrow.visibility = View.VISIBLE
//            alarmDetail.include_alarm_week.visibility = View.GONE
//            alarmDetail.repeat_check_box.isChecked = false
//            alarmDetail.vibration_check_box.isChecked = false
//            viewHolder.alarmView.alarm_switch.isChecked = false
//            for (weekdayButton in weekdayButtons) {
//                weekdayButton.setTextColor(
//                    ContextCompat.getColor(
//                        viewHolder.alarmView.context,
//                        R.color.week_text_selected_color
//                    )
//                )
//                weekdayButton.setBackgroundResource(R.drawable.selected_round_button)
//            }
//            for (weekendButton in weekendButtons) {
//                weekendButton.setTextColor(
//                    ContextCompat.getColor(
//                        viewHolder.alarmView.context,
//                        R.color.week_text_unselected_color
//                    )
//                )
//                weekendButton.setBackgroundResource(R.drawable.unselected_round_button)
//            }
//            alarmDetail.include_alarm_week.sunday_button.setTextColor(
//                ContextCompat.getColor(
//                    viewHolder.alarmView.context,
//                    R.color.week_text_unselected_color
//                )
//            )
//            val adapter = this
//            alarmDetail.include_alarm_week.sunday_button.setBackgroundResource(R.drawable.unselected_round_button)
//            CoroutineScope(Dispatchers.Main).launch {
//                withContext(Dispatchers.Main){
//                    dao.delete(alarms[position])
//                    alarms.removeAt(position)
//                    adapter.notifyItemRemoved(position)
//                    adapter.notifyItemRangeChanged(position, alarms.size)
//                }
//            }
        }
        for (weekdayButton in weekdayButtons) {
            weekButtonSetOnclickListener(weekdayButton, viewHolder)
        }
        for (weekendButton in weekendButtons) {
            weekButtonSetOnclickListener(weekendButton, viewHolder)
        }

        //Select alarm sound
        alarmDetail.sound_button.setOnClickListener{
            val intent = Intent(alarmDetail.context, SoundSelectActivity::class.java)
            alarmDetail.context.startActivity(intent)
        }
    }

    private fun weekButtonSetOnclickListener(weekButton: Button, holder: AlarmViewHolder) {
        weekButton.setOnClickListener {
            if (weekButton.currentTextColor == ContextCompat.getColor(
                    holder.alarmView.context,
                    R.color.week_text_unselected_color
                )
            ) {
                weekButton.setTextColor(
                    ContextCompat.getColor(
                        holder.alarmView.context,
                        R.color.week_text_selected_color
                    )
                )
                weekButton.setBackgroundResource(R.drawable.selected_round_button)
            } else {
                weekButton.setTextColor(
                    ContextCompat.getColor(
                        holder.alarmView.context,
                        R.color.week_text_unselected_color
                    )
                )
                weekButton.setBackgroundResource(R.drawable.unselected_round_button)
            }
        }
    }

    override fun getItemCount() = alarms.size

    fun setItems(alarms: ArrayList<Alarm>){
//        Log.d("size", alarms.size.toString())
//        for(alarm in alarms) {
//            Log.d("id", alarm.id.toString())
//            Log.d("isVibration", alarm.isVibration.toString())
//            Log.d("isRepeatable", alarm.isRepeatable.toString())
//        }
        alarms.sortWith(compareBy({it.hour}, {it.minute}))
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
