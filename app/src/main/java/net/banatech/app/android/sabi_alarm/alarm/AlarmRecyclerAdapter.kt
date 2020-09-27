package net.banatech.app.android.sabi_alarm.alarm

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_detail.view.*
import kotlinx.android.synthetic.main.alarm_view.view.*
import kotlinx.android.synthetic.main.alarm_week.view.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.actions.alarm.AlarmActionsCreator
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm
import net.banatech.app.android.sabi_alarm.sound.SoundSelectActivity
import java.io.IOException
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList


class AlarmRecyclerAdapter(actionsCreator: AlarmActionsCreator) :
    RecyclerView.Adapter<AlarmRecyclerAdapter.AlarmViewHolder>() {
    lateinit var itemListener: OnItemClickListener
    lateinit var soundStartTimeTextListener: OnSoundStartTimeTextClickListener
    private var alarmIdToSoundTestMediaPlayers: Pair<Int, MediaPlayer?> = Pair(0, null)
    private var playingSoundStartButton: ImageButton? = null
    private var playingSoundStopButton: ImageButton? = null

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
            itemListener.onItemClickListener(it, position, alarms[position])
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

        // Change sound start time by number picker
        viewHolder.alarmView.sound_start_time_text.setOnClickListener {
            soundStartTimeTextListener.onSoundStartTimeTextClickListener(
                it,
                position,
                alarms[position]
            )
        }

        // Change sound start time by seek bar
        val soundSeekBar = viewHolder.alarmView.sound_start_time
        try {
            val retriever = MediaMetadataRetriever()
            if (alarms[position].isDefaultSound) {
                val assetFileDescriptor =
                    viewHolder.alarmView.context.assets.openFd("default/${alarms[position].soundFileName}")
                retriever.setDataSource(
                    assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.length
                )
            } else {
                retriever.setDataSource(
                    viewHolder.alarmView.context,
                    Uri.parse(alarms[position].soundFileUri)
                )
            }
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            check(duration is String) { "Duration value must be String" }
            val durationMilli = Integer.parseInt(duration)
            soundSeekBar.max = durationMilli
            soundSeekBar.progress = alarms[position].soundStartTime
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }

        soundSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val sumSeconds = seekBar.progress / 1000
                    val minute = sumSeconds / 60
                    val second = sumSeconds % 60
                    val milli = seekBar.progress % 1000
                    val soundStartTimeText = String.format("%02d:%02d.%03d", minute, second, milli)
                    viewHolder.alarmView.sound_start_time_text.text = soundStartTimeText
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val sumSeconds = seekBar.progress / 1000
                    val minute = sumSeconds / 60
                    val second = sumSeconds % 60
                    val milli = seekBar.progress % 1000
                    val soundStartTimeText = String.format("%02d:%02d.%03d", minute, second, milli)
                    viewHolder.alarmView.sound_start_time_text.text = soundStartTimeText
                    actionsCreator.changeSoundStartTime(
                        alarms[position].id,
                        seekBar.progress,
                        soundStartTimeText,
                        viewHolder.alarmView.context
                    )
                }
            }
        )

        val alarmDetail = viewHolder.alarmView.include_alarm_detail
        val alarmSwitch = viewHolder.alarmView.alarm_switch

        // Sound test play
        alarmDetail.sound_play_button.setOnClickListener {
            soundPlayAndStop(
                alarms[position],
                viewHolder.alarmView.context,
                viewHolder.alarmView.sound_play_button,
                viewHolder.alarmView.sound_stop_button
            )
        }
        alarmDetail.sound_stop_button.setOnClickListener {
            soundPlayAndStop(
                alarms[position],
                viewHolder.alarmView.context,
                viewHolder.alarmView.sound_play_button,
                viewHolder.alarmView.sound_stop_button
            )
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
        val vibrator = getSystemService(viewHolder.alarmView.context, Vibrator::class.java)
        val vibrationEffect = VibrationEffect.createOneShot(300, DEFAULT_AMPLITUDE)
        alarmDetail.vibration_check_box.setOnClickListener {
            actionsCreator.switchVibration(
                alarms[position].id,
                alarmDetail.vibration_check_box.isChecked,
                viewHolder.alarmView.context
            )
            if (alarms[position].isVibration) {
                vibrator?.vibrate(vibrationEffect)
            }
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
            }
            if (weekAlarmArray[i]) {
                selectWeekButton(weekButtonList[i], viewHolder)
            } else {
                unselectWeekButton(weekButtonList[i], viewHolder)
            }
        }

        //Destroy alarm
        alarmDetail.delete_button.setOnClickListener {
            stopPlayingSound(
                viewHolder.alarmView.sound_play_button,
                viewHolder.alarmView.sound_stop_button
            )
            alarmDetail.visibility = View.GONE
            viewHolder.alarmView.alarm_down_arrow.visibility = View.VISIBLE
            alarmDetail.repeat_check_box.isChecked = false
            alarmDetail.include_alarm_week.visibility = View.GONE
            alarmDetail.vibration_check_box.isChecked = false
            alarmSwitch.isChecked = true
            soundSeekBar.progress = 0
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
            stopPlayingSound(
                viewHolder.alarmView.sound_play_button,
                viewHolder.alarmView.sound_stop_button
            )
            val intent = Intent(alarmDetail.context, SoundSelectActivity::class.java).apply {
                putExtra("ALARM_ID", alarms[position].id)
            }
            alarmDetail.context.startActivity(intent)
        }
        alarmDetail.sound_button.text = alarms[position].soundFileName

        // TODO The sabi detection function will have to wait
//        alarmDetail.sabi_detect_button.setOnClickListener {
//            stopPlayingSound(viewHolder.alarmView.sound_play_button, viewHolder.alarmView.sound_stop_button)
//            Detector.detect(Uri.parse(""), viewHolder.alarmView.context.assets)
//        }
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

    private fun soundPlayAndStop(
        alarm: Alarm,
        context: Context,
        soundStartButton: ImageButton,
        soundStopButton: ImageButton
    ) {
        if (alarmIdToSoundTestMediaPlayers.second != null && alarmIdToSoundTestMediaPlayers.first == alarm.id) {
            val mediaPlayer = alarmIdToSoundTestMediaPlayers.second
            check(mediaPlayer != null) { "mediaPlayer must not be null" }
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
            soundStartButton.visibility = View.VISIBLE
            soundStopButton.visibility = View.INVISIBLE
            alarmIdToSoundTestMediaPlayers = Pair(0, null)
        } else if (alarmIdToSoundTestMediaPlayers.second == null) {
            val mediaPlayer = MediaPlayer()
            alarmIdToSoundTestMediaPlayers = Pair(alarm.id, mediaPlayer)
            this.playingSoundStartButton = soundStartButton
            this.playingSoundStopButton = soundStopButton
            if (alarm.isDefaultSound) {
                val fileName = "default/${alarm.soundFileName}"
                val assetFileDescriptor = context.assets.openFd(fileName)
                try {
                    mediaPlayer.setDataSource(assetFileDescriptor)
                    mediaPlayer.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(alarm.soundStartTime)
                    mediaPlayer.start()
                    soundStartButton.visibility = View.INVISIBLE
                    soundStopButton.visibility = View.VISIBLE
                    mediaPlayer.setOnCompletionListener {
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val soundFinishAction =
                            sharedPreferences.getString("sound_finish_action", "0")?.toInt() ?: 0
                        when (soundFinishAction) {
                            0 -> {
                                it.seekTo(alarm.soundStartTime)
                                it.start()
                            }
                            1 -> {
                                it.seekTo(0)
                                it.start()
                            }
                            2 -> {
                                it.release()
                                alarmIdToSoundTestMediaPlayers = Pair(0, null)
                                soundStartButton.visibility = View.VISIBLE
                                soundStopButton.visibility = View.INVISIBLE
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                val fileUri = Uri.parse(alarm.soundFileUri)
                try {
                    mediaPlayer.setDataSource(context, fileUri)
                    mediaPlayer.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(alarm.soundStartTime)
                    mediaPlayer.start()
                    soundStartButton.visibility = View.INVISIBLE
                    soundStopButton.visibility = View.VISIBLE
                    mediaPlayer.setOnCompletionListener {
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val soundFinishAction =
                            sharedPreferences.getString("sound_finish_action", "0")?.toInt() ?: 0
                        when (soundFinishAction) {
                            0 -> {
                                it.seekTo(alarm.soundStartTime)
                                it.start()
                            }
                            1 -> {
                                it.seekTo(0)
                                it.start()
                            }
                            2 -> {
                                it.release()
                                alarmIdToSoundTestMediaPlayers = Pair(0, null)
                                soundStartButton.visibility = View.VISIBLE
                                soundStopButton.visibility = View.INVISIBLE
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            val existMediaPlayer = alarmIdToSoundTestMediaPlayers.second
            check(existMediaPlayer != null) { "existMediaPlayer must not be null" }
            existMediaPlayer.stop()
            existMediaPlayer.reset()
            existMediaPlayer.release()
            this.playingSoundStartButton?.visibility = View.VISIBLE
            this.playingSoundStopButton?.visibility = View.INVISIBLE
            soundStartButton.visibility = View.INVISIBLE
            soundStopButton.visibility = View.VISIBLE
            val mediaPlayer = MediaPlayer()
            alarmIdToSoundTestMediaPlayers = Pair(alarm.id, mediaPlayer)
            this.playingSoundStartButton = soundStartButton
            this.playingSoundStopButton = soundStopButton
            if (alarm.isDefaultSound) {
                val fileName = "default/${alarm.soundFileName}"
                val assetFileDescriptor = context.assets.openFd(fileName)
                try {
                    mediaPlayer.setDataSource(assetFileDescriptor)
                    mediaPlayer.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(alarm.soundStartTime)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val soundFinishAction =
                            sharedPreferences.getString("sound_finish_action", "0")?.toInt() ?: 0
                        when (soundFinishAction) {
                            0 -> {
                                it.seekTo(alarm.soundStartTime)
                                it.start()
                            }
                            1 -> {
                                it.seekTo(0)
                                it.start()
                            }
                            2 -> {
                                it.release()
                                alarmIdToSoundTestMediaPlayers = Pair(0, null)
                                soundStartButton.visibility = View.VISIBLE
                                soundStopButton.visibility = View.INVISIBLE
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                val fileUri = Uri.parse(alarm.soundFileUri)
                try {
                    mediaPlayer.setDataSource(context, fileUri)
                    mediaPlayer.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(alarm.soundStartTime)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val soundFinishAction =
                            sharedPreferences.getString("sound_finish_action", "0")?.toInt() ?: 0
                        when (soundFinishAction) {
                            0 -> {
                                it.seekTo(alarm.soundStartTime)
                                it.start()
                            }
                            1 -> {
                                it.seekTo(0)
                                it.start()
                            }
                            2 -> {
                                it.release()
                                alarmIdToSoundTestMediaPlayers = Pair(0, null)
                                soundStartButton.visibility = View.VISIBLE
                                soundStopButton.visibility = View.INVISIBLE
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun stopPlayingSound(soundPlayButton: ImageButton, soundStopButton: ImageButton) {
        if (alarmIdToSoundTestMediaPlayers.second != null) {
            val mediaPlayer = alarmIdToSoundTestMediaPlayers.second
            check(mediaPlayer != null) { "mediaPlayer must not be null" }
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
            soundPlayButton.visibility = View.VISIBLE
            soundStopButton.visibility = View.INVISIBLE
            alarmIdToSoundTestMediaPlayers = Pair(0, null)
        }
    }

    override fun getItemCount() = alarms.size

    fun setItems(alarms: ArrayList<Alarm>, alarmSortPreferenceValue: Int) {
        when (alarmSortPreferenceValue) {
            0 -> {
                alarms.sortWith(compareBy({ it.hour }, { it.minute }))
                this.alarms = alarms
            }
            1 -> {
                alarms.sortWith(compareBy({ -it.hour }, { -it.minute }))
                this.alarms = alarms
            }
            2 -> {
                this.alarms = alarms
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int, alarm: Alarm)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemListener = listener
    }

    interface OnSoundStartTimeTextClickListener {
        fun onSoundStartTimeTextClickListener(view: View, position: Int, alarm: Alarm)
    }

    fun setOnSoundStartTimeTextClickListener(listener: OnSoundStartTimeTextClickListener) {
        this.soundStartTimeTextListener = listener
    }
}
