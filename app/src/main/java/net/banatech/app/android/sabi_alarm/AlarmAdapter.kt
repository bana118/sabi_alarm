package net.banatech.app.android.sabi_alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_detail.view.*
import kotlinx.android.synthetic.main.alarm_view.view.*
import kotlinx.android.synthetic.main.alarm_week.view.*

class AlarmAdapter(private val timeDataset: ArrayList<Alarm>) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {
    lateinit var listener: OnItemClickListener

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class AlarmViewHolder(val alarmView: View) : RecyclerView.ViewHolder(alarmView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlarmViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_view, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return AlarmViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.alarmView.text_view.text = timeDataset[position].timeText
        holder.alarmView.setOnClickListener {
            listener.onItemClickListener(it, position, timeDataset[position].timeText)
        }
        val alarmDetail = holder.alarmView.include_alarm_detail
        alarmDetail.repeat_check_box.setOnClickListener {
            if (alarmDetail.repeat_check_box.isChecked) {
                alarmDetail.include_alarm_week.visibility = View.VISIBLE
            } else {
                alarmDetail.include_alarm_week.visibility = View.GONE
            }
        }

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
        alarmDetail.delete_button.setOnClickListener {
            alarmDetail.visibility = View.GONE
            alarmDetail.repeat_check_box.isChecked = false
            alarmDetail.vibration_check_box.isChecked = false
            for (weekdayButton in weekdayButtons) {
                weekdayButton.setTextColor(
                    ContextCompat.getColor(
                        holder.alarmView.context,
                        R.color.week_text_selected_color
                    )
                )
                weekdayButton.setBackgroundResource(R.drawable.selected_round_button)
            }
            for (weekendButton in weekendButtons) {
                weekendButton.setTextColor(
                    ContextCompat.getColor(
                        holder.alarmView.context,
                        R.color.week_text_unselected_color
                    )
                )
                weekendButton.setBackgroundResource(R.drawable.unselected_round_button)
            }
            alarmDetail.include_alarm_week.sunday_button.setTextColor(
                ContextCompat.getColor(
                    holder.alarmView.context,
                    R.color.week_text_unselected_color
                )
            )
            alarmDetail.include_alarm_week.sunday_button.setBackgroundResource(R.drawable.unselected_round_button)
            timeDataset.removeAt(position)
            this.notifyItemRemoved(position)
            this.notifyItemRangeChanged(position, timeDataset.size)
        }
        for (weekdayButton in weekdayButtons) {
            weekButtonSetOnclickListener(weekdayButton, holder)
        }
        for (weekendButton in weekendButtons) {
            weekButtonSetOnclickListener(weekendButton, holder)
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

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = timeDataset.size

    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int, clickedText: String)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}