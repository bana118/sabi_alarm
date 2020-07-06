package net.banatech.app.android.sabi_alarm.sound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sound_file_view.view.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.actions.ActionsCreator
import net.banatech.app.android.sabi_alarm.alarm.stores.AlarmStore

class LocalSoundRecyclerAdapter(private val localAlarmSoundArray: Array<String>) :
    RecyclerView.Adapter<LocalSoundRecyclerAdapter.LocalFileViewHolder>() {

    class LocalFileViewHolder(val soundFileView: View) : RecyclerView.ViewHolder(soundFileView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocalFileViewHolder {
        // create a new view
        val localFileView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sound_file_view, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return LocalFileViewHolder(
            localFileView
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: LocalFileViewHolder, position: Int) {
        holder.soundFileView.sound_file_name.text = localAlarmSoundArray[position]
        val checkBox = holder.soundFileView.sound_file_check
        holder.soundFileView.sound_file_layout.setOnClickListener {
            ActionsCreator.selectSound(AlarmStore.selectedAlarm.id, localAlarmSoundArray[position])
            if(AlarmStore.selectedAlarm.soundFileName == localAlarmSoundArray[position]){
                checkBox.visibility = View.VISIBLE
            }else{
                checkBox.visibility = View.INVISIBLE
            }
            for(i in localAlarmSoundArray.indices){
                if(i != position){
                    notifyItemChanged(i)
                }
            }
        }
        if(AlarmStore.selectedAlarm.soundFileName == localAlarmSoundArray[position]){
            checkBox.visibility = View.VISIBLE
        }else{
            checkBox.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = localAlarmSoundArray.size
}
