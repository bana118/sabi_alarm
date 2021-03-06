package net.banatech.app.android.sabi_alarm.sound

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.default_sound_file_view.view.sound_file_check
import kotlinx.android.synthetic.main.default_sound_file_view.view.sound_file_name
import kotlinx.android.synthetic.main.local_sound_file_view.view.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.actions.alarm.AlarmActionsCreator
import net.banatech.app.android.sabi_alarm.actions.sound.SoundActionsCreator
import net.banatech.app.android.sabi_alarm.sound.database.Sound
import net.banatech.app.android.sabi_alarm.stores.alarm.AlarmStore

class LocalSoundRecyclerAdapter(
    actionsCreator: SoundActionsCreator,
    private val defaultSoundAdapter: DefaultSoundRecyclerAdapter
) :
    RecyclerView.Adapter<LocalSoundRecyclerAdapter.LocalFileViewHolder>() {

    companion object {
        lateinit var actionsCreator: SoundActionsCreator

        fun isAvailable(uri: Uri, context: Context): Boolean {
            val file = DocumentFile.fromSingleUri(context, uri)
            return file != null && file.canRead()
        }

        fun setDefaultSound(alarmId: Int, context: Context) {
            val assetManager = context.resources.assets
            val defaultSoundDir = assetManager.list("default")
            check(defaultSoundDir != null) { "default sound list must not be null" }
            AlarmActionsCreator.selectSound(
                alarmId,
                defaultSoundDir.first(),
                true,
                "",
                context
            )
        }
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

        // select sound
        holder.soundFileView.local_sound_file_layout.setOnClickListener {
            val isAvailable =
                isAvailable(Uri.parse(sounds[position].stringUri), holder.soundFileView.context)
            if (isAvailable) {
                AlarmActionsCreator.selectSound(
                    AlarmStore.selectedAlarm.id,
                    sounds[position].fileName,
                    false,
                    sounds[position].stringUri,
                    holder.soundFileView.context
                )
                if (AlarmStore.selectedAlarm.soundFileName == sounds[position].fileName) {
                    checkBox.visibility = View.VISIBLE
                } else {
                    checkBox.visibility = View.INVISIBLE
                }
                for (i in sounds.indices) {
                    if (i != position) {
                        notifyItemChanged(i)
                    }
                }
            } else {
                Toast.makeText(
                    holder.soundFileView.context,
                    "音楽ファイルの読み込みに失敗しました",
                    Toast.LENGTH_SHORT
                ).show()
                setDefaultSound(AlarmStore.selectedAlarm.id, holder.soundFileView.context)
                notifyDataSetChanged()
            }
            defaultSoundAdapter.notifyDataSetChanged()
        }
        if (!AlarmStore.selectedAlarm.isDefaultSound && AlarmStore.selectedAlarm.soundFileUri == sounds[position].stringUri) {
            checkBox.visibility = View.VISIBLE
        } else {
            checkBox.visibility = View.INVISIBLE
        }

        // delete sound
        holder.soundFileView.delete_button.setOnClickListener {
            if (AlarmStore.selectedAlarm.soundFileName == sounds[position].fileName) {
                AlarmActionsCreator.selectSound(
                    AlarmStore.selectedAlarm.id,
                    "beethoven_no5_1st.mp3",
                    true,
                    "",
                    holder.soundFileView.context
                )
            }
            SoundActionsCreator.remove(sounds[position].id, holder.soundFileView.context)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, sounds.size)
            defaultSoundAdapter.notifyDataSetChanged()
        }
    }

    fun setItems(sounds: ArrayList<Sound>, soundSortPreferenceValue: Int) {
        when (soundSortPreferenceValue) {
            0 -> {
                sounds.sortWith(compareBy({ it.fileName }, { it.stringUri }))
                this.sounds = sounds
            }
            1 -> {
                sounds.sortWith(compareBy({ it.fileName }, { it.stringUri }))
                sounds.reverse()
                this.sounds = sounds
            }
            2 -> {
                this.sounds = sounds
            }
        }
        this.sounds = sounds
    }

    override fun getItemCount() = sounds.size
}
