package net.banatech.app.android.sabi_alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_view.view.*

class AlarmAdapter(private val timeDataset: ArrayList<String>) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {
    lateinit var listener: OnItemClickListener

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class AlarmViewHolder(val alarmView: View) : RecyclerView.ViewHolder(alarmView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AlarmViewHolder {
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
        holder.alarmView.text_view.text = timeDataset[position]
        holder.alarmView.setOnClickListener {
            listener.onItemClickListener(it, position, timeDataset[position])
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = timeDataset.size


    interface OnItemClickListener{
        fun onItemClickListener(view: View, position: Int, clickedText: String)
    }


    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }
}