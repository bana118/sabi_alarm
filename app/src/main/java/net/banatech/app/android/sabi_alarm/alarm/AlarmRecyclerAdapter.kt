package net.banatech.app.android.sabi_alarm.alarm

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_detail.view.*
import kotlinx.android.synthetic.main.alarm_view.view.*
import kotlinx.android.synthetic.main.alarm_week.view.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.actions.alarm.AlarmActionsCreator
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import net.banatech.app.android.sabi_alarm.sound.SoundSelectActivity
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class AlarmRecyclerAdapter(actionsCreator: AlarmActionsCreator) :
    RecyclerView.Adapter<AlarmRecyclerAdapter.AlarmViewHolder>() {
    lateinit var listener: OnItemClickListener
    private var alarmIdToSoundTestMediaPlayers: Pair<Int, MediaPlayer?> = Pair(0, null)

    companion object {
        lateinit var actionsCreator: AlarmActionsCreator
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
        //Show alarm detail
        viewHolder.alarmView.setOnClickListener {
            listener.onItemClickListener(it, position, alarms[position])
        }

        //Edit alarm time
        viewHolder.alarmView.alarm_time.text = alarms[position].timeText
        viewHolder.alarmView.alarm_time.setOnClickListener {
            val hour = alarms[position].hour
            val minute = alarms[position].minute
            val timePickerDialog = TimePickerDialog(
                viewHolder.alarmView.context,
                TimePickerDialog.OnTimeSetListener { _: TimePicker, pickerHour: Int, pickerMinute: Int ->
                    actionsCreator.edit(
                        alarms[position].id,
                        pickerHour,
                        pickerMinute,
                        viewHolder.alarmView.context
                    )
                    notifyDataSetChanged()
                },
                hour, minute, true
            )
            timePickerDialog.show()
        }

        //Change sound start time
        viewHolder.alarmView.sound_start_time_text.text = alarms[position].soundStartTimeText
        val soundSeekBar = viewHolder.alarmView.sound_start_time
        val retriever = MediaMetadataRetriever()
        if (alarms[position].isDefaultSound) {
            val assetFileDescriptor =
                viewHolder.alarmView.context.assets.openFd("default/${alarms[position].soundFileName}")
            retriever.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
        }else{
            retriever.setDataSource(viewHolder.alarmView.context, Uri.parse(alarms[position].soundFileUri))
        }
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val durationMilli = Integer.parseInt(duration)
        soundSeekBar.max = durationMilli
        soundSeekBar.progress = alarms[position].soundStartTime
        soundSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val sumSeconds = progress / 1000
                    val minute = sumSeconds / 60
                    val second = sumSeconds % 60
                    val milli = progress % 1000
                    val soundStartTimeText = String.format("%02d:%02d.%03d", minute, second, milli)
                    viewHolder.alarmView.sound_start_time_text.text = soundStartTimeText
                    actionsCreator.changeSoundStartTime(
                        alarms[position].id,
                        progress,
                        soundStartTimeText
                    )
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            }
        )

        val alarmDetail = viewHolder.alarmView.include_alarm_detail
        val alarmSwitch = viewHolder.alarmView.alarm_switch

        // Sound test play
        alarmDetail.test_play_button.setOnClickListener {
            soundPlayAndStop(alarms[position], viewHolder.alarmView.context)
        }

        //Switch alarm on/off
        alarmSwitch.setOnClickListener {
            actionsCreator.switchEnable(
                alarms[position].id,
                alarmSwitch.isChecked,
                viewHolder.alarmView.context
            )
        }
        alarmSwitch.isChecked = alarms[position].enable

        if (alarms[position].isShowDetail) {
            alarmDetail.visibility = View.VISIBLE
            viewHolder.alarmView.alarm_down_arrow.visibility = View.GONE
        } else {
            alarmDetail.visibility = View.GONE
            viewHolder.alarmView.alarm_down_arrow.visibility = View.VISIBLE
        }

        //Switch alarm vibration
        alarmDetail.vibration_check_box.setOnClickListener {
            actionsCreator.switchVibration(
                alarms[position].id,
                alarmDetail.vibration_check_box.isChecked
            )
        }
        alarmDetail.vibration_check_box.isChecked = alarms[position].isVibration

        //Switch alarm repeatable
        alarmDetail.repeat_check_box.setOnClickListener {
            actionsCreator.switchRepeatable(
                alarms[position].id,
                alarmDetail.repeat_check_box.isChecked,
                viewHolder.alarmView.context
            )
            if (alarms[position].isRepeatable) {
                alarmDetail.include_alarm_week.visibility = View.VISIBLE
            } else {
                alarmDetail.include_alarm_week.visibility = View.GONE
            }
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
        for (i in 0 until 7) {
            weekButtonList[i].setOnClickListener {
                actionsCreator.switchDayAlarm(
                    alarms[position].id,
                    weekList[i],
                    !weekAlarmArray[i],
                    viewHolder.alarmView.context
                )
                weekAlarmArray[i] = !weekAlarmArray[i]
                if (weekAlarmArray[i]) {
                    selectWeekButton(weekButtonList[i], viewHolder)
                } else {
                    unselectWeekButton(weekButtonList[i], viewHolder)
                }
                if (!alarms[position].isRepeatable) {
                    alarmDetail.repeat_check_box.isChecked = false
                    alarmDetail.include_alarm_week.visibility = View.GONE
                    weekAlarmArray[i] = !weekAlarmArray[i]
                    selectWeekButton(weekButtonList[i], viewHolder)
                }
                Log.d(
                    "debug",
                    "repeatable${alarms[position].isRepeatable}, week${weekAlarmArray[i]}"
                )
            }
            if (weekAlarmArray[i]) {
                selectWeekButton(weekButtonList[i], viewHolder)
            } else {
                unselectWeekButton(weekButtonList[i], viewHolder)
            }
        }

        //Destroy alarm
        alarmDetail.delete_button.setOnClickListener {
            stopPlayingSound()
            alarmDetail.visibility = View.GONE
            viewHolder.alarmView.alarm_down_arrow.visibility = View.VISIBLE
            alarmDetail.repeat_check_box.isChecked = false
            alarmDetail.include_alarm_week.visibility = View.GONE
            alarmDetail.vibration_check_box.isChecked = false
            alarmSwitch.isChecked = true
            for (i in 0 until 7) {
                if (i == 0 || i == 6) {
                    unselectWeekButton(weekButtonList[i], viewHolder)
                } else {
                    selectWeekButton(weekButtonList[i], viewHolder)
                }
            }
            actionsCreator.destroy(alarms[position].id, viewHolder.alarmView.context)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, alarms.size)
        }

        //Select alarm sound
        alarmDetail.sound_button.setOnClickListener {
            stopPlayingSound()
            val intent = Intent(alarmDetail.context, SoundSelectActivity::class.java).apply {
                putExtra("ALARM_ID", alarms[position].id)
            }
            alarmDetail.context.startActivity(intent)
        }
        alarmDetail.sound_button.text = alarms[position].soundFileName
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

    private fun soundPlayAndStop(alarm: Alarm, context: Context) {
        if (alarmIdToSoundTestMediaPlayers.second != null && alarmIdToSoundTestMediaPlayers.first == alarm.id) {
            val mediaPlayer = alarmIdToSoundTestMediaPlayers.second
            check(mediaPlayer != null) { "mediaPlayer must not be null" }
            mediaPlayer.stop()
            mediaPlayer.release()
            alarmIdToSoundTestMediaPlayers = Pair(0, null)
        } else if(alarmIdToSoundTestMediaPlayers.second == null) {
            val mediaPlayer = MediaPlayer()
            alarmIdToSoundTestMediaPlayers = Pair(alarm.id, mediaPlayer)
            if(alarm.isDefaultSound){
                val fileName =  "default/${alarm.soundFileName}"
                val assetFileDescriptor = context.assets.openFd(fileName)
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(assetFileDescriptor)
                    mediaPlayer.setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(alarm.soundStartTime)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        it.release()
                        alarmIdToSoundTestMediaPlayers = Pair(0, null)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }else{
                val fileUri =  Uri.parse(alarm.soundFileUri)
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(context, fileUri)
                    mediaPlayer.setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(alarm.soundStartTime)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        it.release()
                        alarmIdToSoundTestMediaPlayers = Pair(0, null)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }else{
            val existMediaPlayer = alarmIdToSoundTestMediaPlayers.second
            check(existMediaPlayer != null) { "existMediaPlayer must not be null" }
            existMediaPlayer.stop()
            existMediaPlayer.release()
            val mediaPlayer = MediaPlayer()
            alarmIdToSoundTestMediaPlayers = Pair(alarm.id, mediaPlayer)
            if(alarm.isDefaultSound){
                val fileName = "default/${alarm.soundFileName}"
                val assetFileDescriptor = context.assets.openFd(fileName)
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(assetFileDescriptor)
                    mediaPlayer.setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(alarm.soundStartTime)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        it.release()
                        alarmIdToSoundTestMediaPlayers = Pair(0, null)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }else{
                val fileUri = Uri.parse(alarm.soundFileUri)
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(context, fileUri)
                    mediaPlayer.setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(alarm.soundStartTime)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        it.release()
                        alarmIdToSoundTestMediaPlayers = Pair(0, null)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun stopPlayingSound(){
        if(alarmIdToSoundTestMediaPlayers.second != null){
            val mediaPlayer = alarmIdToSoundTestMediaPlayers.second
            check(mediaPlayer != null) { "mediaPlayer must not be null" }
            mediaPlayer.stop()
            mediaPlayer.release()
            alarmIdToSoundTestMediaPlayers = Pair(0, null)
        }
    }

    override fun getItemCount() = alarms.size

    fun setItems(alarms: ArrayList<Alarm>) {
        alarms.sortWith(compareBy({ it.hour }, { it.minute }))
        this.alarms = alarms
    }

    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int, alarm: Alarm)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}
