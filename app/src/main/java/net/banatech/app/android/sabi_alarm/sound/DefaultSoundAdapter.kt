package net.banatech.app.android.sabi_alarm.sound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sound_file_view.view.*
import net.banatech.app.android.sabi_alarm.R

class DefaultSoundAdapter(private val defaultSoundFileList: Array<String>?) :
    RecyclerView.Adapter<DefaultSoundAdapter.DefaultSoundViewHolder>()  {

    class DefaultSoundViewHolder(val soundFileView: View) : RecyclerView.ViewHolder(soundFileView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DefaultSoundViewHolder {
        // create a new view
        val soundFileView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sound_file_view, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return DefaultSoundViewHolder(
            soundFileView
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: DefaultSoundViewHolder, position: Int) {
        val assetManager = holder.soundFileView.resources.assets
        if(defaultSoundFileList != null){
            //val soundFile = assetManager.open(defaultSoundFileList[position])
            holder.soundFileView.sound_file_name.text = defaultSoundFileList[position]
        }
    }

    override fun getItemCount() = defaultSoundFileList?.size ?: 0
}