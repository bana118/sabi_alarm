package net.banatech.app.android.sabi_alarm.sound

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.default_sound_file_view.view.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.actions.alarm.AlarmActionsCreator
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore

class DefaultSoundRecyclerAdapter(private val defaultAlarmSoundArray: Array<String>) :
    RecyclerView.Adapter<DefaultSoundRecyclerAdapter.DefaultSoundViewHolder>() {

    class DefaultSoundViewHolder(val soundFileView: View) : RecyclerView.ViewHolder(soundFileView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultSoundViewHolder {
        // create a new view
        val soundFileView = LayoutInflater.from(parent.context)
            .inflate(R.layout.default_sound_file_view, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return DefaultSoundViewHolder(
            soundFileView
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: DefaultSoundViewHolder, position: Int) {
        holder.soundFileView.sound_file_name.text = defaultAlarmSoundArray[position]
        val checkBox = holder.soundFileView.sound_file_check
        holder.soundFileView.default_sound_file_layout.setOnClickListener {
            AlarmActionsCreator.selectSound(AlarmStore.selectedAlarm.id, defaultAlarmSoundArray[position], true)
            if(AlarmStore.selectedAlarm.soundFileName == defaultAlarmSoundArray[position]){
                checkBox.visibility = View.VISIBLE
            }else{
                checkBox.visibility = View.INVISIBLE
            }
            for(i in defaultAlarmSoundArray.indices){
                if(i != position){
                    notifyItemChanged(i)
                }
            }
            Log.d("selected", AlarmStore.selectedAlarm.soundFileName)
            Log.d("position sound", defaultAlarmSoundArray[position])
        }
        if(AlarmStore.selectedAlarm.soundFileName == defaultAlarmSoundArray[position]){
            checkBox.visibility = View.VISIBLE
        }else{
            checkBox.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = defaultAlarmSoundArray.size
}
