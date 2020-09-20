package net.banatech.app.android.sabi_alarm.sound

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.default_sound_file_view.view.*
import kotlinx.android.synthetic.main.default_sound_file_view.view.sound_file_check
import kotlinx.android.synthetic.main.default_sound_file_view.view.sound_file_name
import kotlinx.android.synthetic.main.local_sound_file_view.view.*
import kotlinx.android.synthetic.main.sound_select.view.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.actions.alarm.AlarmActionsCreator
import net.banatech.app.android.sabi_alarm.actions.sound.SoundActionsCreator
import net.banatech.app.android.sabi_alarm.sound.database.Sound
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore

class LocalSoundRecyclerAdapter(actionsCreator: SoundActionsCreator, private val defaultSoundAdapter: DefaultSoundRecyclerAdapter) :
    RecyclerView.Adapter<LocalSoundRecyclerAdapter.LocalFileViewHolder>() {

    companion object {
        lateinit var actionsCreator: SoundActionsCreator
    }

    private var sounds: ArrayList<Sound> = ArrayList()

    init {
        LocalSoundRecyclerAdapter.actionsCreator = actionsCreator
    }
    class LocalFileViewHolder(val soundFileView: View) : RecyclerView.ViewHolder(soundFileView)

    override fun getItemId(position: Int): Long {
        setHasStableIds(true)
        return sounds[position].id.toLong()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocalFileViewHolder {
        val localFileView = LayoutInflater.from(parent.context)
            .inflate(R.layout.local_sound_file_view, parent, false)
        return LocalFileViewHolder(
            localFileView
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: LocalFileViewHolder, position: Int) {
        holder.soundFileView.sound_file_name.text = sounds[position].fileName
        val checkBox = holder.soundFileView.sound_file_check
        holder.soundFileView.local_sound_file_layout.setOnClickListener {
            AlarmActionsCreator.selectSound(AlarmStore.selectedAlarm.id, sounds[position].fileName, false, sounds[position].stringUri)
            if(AlarmStore.selectedAlarm.soundFileName == sounds[position].fileName){
                checkBox.visibility = View.VISIBLE
            }else{
                checkBox.visibility = View.INVISIBLE
            }
            for(i in sounds.indices){
                if(i != position){
                    notifyItemChanged(i)
                }
            }
            defaultSoundAdapter.notifyDataSetChanged()
        }
        if(AlarmStore.selectedAlarm.soundFileName == sounds[position].fileName){
            checkBox.visibility = View.VISIBLE
        }else{
            checkBox.visibility = View.INVISIBLE
        }
        holder.soundFileView.delete_button.setOnClickListener{
            if(AlarmStore.selectedAlarm.soundFileName == sounds[position].fileName){
                AlarmActionsCreator.selectSound(AlarmStore.selectedAlarm.id, "beethoven_no5_1st.mp3", true, "")
            }
            SoundActionsCreator.remove(sounds[position].id, holder.soundFileView.context)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, sounds.size)
            defaultSoundAdapter.notifyDataSetChanged()
        }
    }
    fun setItems(sounds: ArrayList<Sound>) {
        this.sounds = sounds
    }

    override fun getItemCount() = sounds.size
}
