package net.banatech.app.android.sabi_alarm.alarm

import android.app.Dialog
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.actions.alarm.AlarmActionsCreator
import net.banatech.app.android.sabi_alarm.alarm.database.Alarm

class NumberPickerDialog : DialogFragment() {
    private var initMinutes: Int? = null
    private var initSeconds: Int? = null
    private var initMillis: Int? = null
    lateinit var alarm: Alarm
    lateinit var viewContext: Context
    lateinit var listAdapter: AlarmRecyclerAdapter
    private var durationMilli: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val numberPickerDialog = inflater.inflate(R.layout.number_picker_dialog, null)
            val minutesPicker = numberPickerDialog.findViewById<NumberPicker>(R.id.minutesPicker)
            val secondsPicker = numberPickerDialog.findViewById<NumberPicker>(R.id.secondsPicker)
            val millisPicker = numberPickerDialog.findViewById<NumberPicker>(R.id.millisPicker)

            val dialogLimits = calcDialogLimits(initMinutes ?: 0, initSeconds ?: 0)
            minutesPicker.minValue = 0
            minutesPicker.maxValue = dialogLimits["maxMinutes"] ?: 99
            secondsPicker.minValue = 0
            secondsPicker.maxValue = dialogLimits["maxSeconds"] ?: 59
            millisPicker.minValue = 0
            millisPicker.maxValue = dialogLimits["maxMillis"] ?: 999

            minutesPicker.value = initMinutes ?: 0
            secondsPicker.value = initSeconds ?: 0
            millisPicker.value = initMillis ?: 0

            minutesPicker.setOnValueChangedListener { picker, oldValue, newValue ->
                val soundStartTimeMillis =
                    newValue * 60 * 1000 + secondsPicker.value * 1000 + millisPicker.value
                val soundStartTimeText = String.format(
                    "%02d:%02d.%03d",
                    newValue,
                    secondsPicker.value,
                    millisPicker.value
                )
                AlarmActionsCreator.changeSoundStartTime(
                    this.alarm.id,
                    soundStartTimeMillis,
                    soundStartTimeText,
                    this.viewContext
                )
                secondsPicker.maxValue =
                    calcDialogLimits(newValue, secondsPicker.value)["maxSeconds"] ?: 59
                this.listAdapter.notifyDataSetChanged()
            }
            secondsPicker.setOnValueChangedListener { picker, oldValue, newValue ->
                val soundStartTimeMillis =
                    minutesPicker.value * 60 * 1000 + newValue * 1000 + millisPicker.value
                val soundStartTimeText = String.format(
                    "%02d:%02d.%03d",
                    minutesPicker.value,
                    newValue,
                    millisPicker.value
                )
                AlarmActionsCreator.changeSoundStartTime(
                    this.alarm.id,
                    soundStartTimeMillis,
                    soundStartTimeText,
                    this.viewContext
                )
                millisPicker.maxValue =
                    calcDialogLimits(minutesPicker.value, newValue)["maxMillis"] ?: 999
                this.listAdapter.notifyDataSetChanged()
            }
            millisPicker.setOnValueChangedListener { picker, oldValue, newValue ->
                val soundStartTimeMillis =
                    minutesPicker.value * 60 * 1000 + secondsPicker.value * 1000 + newValue
                val soundStartTimeText = String.format(
                    "%02d:%02d.%03d",
                    minutesPicker.value,
                    secondsPicker.value,
                    newValue
                )
                AlarmActionsCreator.changeSoundStartTime(
                    this.alarm.id,
                    soundStartTimeMillis,
                    soundStartTimeText,
                    this.viewContext
                )
                this.listAdapter.notifyDataSetChanged()
            }

            builder.setView(numberPickerDialog)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun calcDialogLimits(minutes: Int, seconds: Int): Map<String, Int> {
        val maxMinutes = this.durationMilli?.div(60 * 1000) ?: 99
        val maxSeconds = if (minutes == maxMinutes) {
            this.durationMilli?.div(1000)?.rem(60) ?: 59
        } else {
            59
        }
        val maxMillis = if (minutes == maxMinutes && seconds == maxSeconds) {
            this.durationMilli?.rem(1000) ?: 999
        } else {
            999
        }
        return mapOf(
            "maxMinutes" to maxMinutes,
            "maxSeconds" to maxSeconds,
            "maxMillis" to maxMillis
        )
    }

    fun setDialogInit(
        alarm: Alarm,
        initMinutes: Int,
        initSeconds: Int,
        initMillis: Int,
        viewContext: Context,
        listAdapter: AlarmRecyclerAdapter
    ) {
        this.alarm = alarm
        this.initMinutes = initMinutes
        this.initSeconds = initSeconds
        this.initMillis = initMillis
        val retriever = MediaMetadataRetriever()
        if (alarm.isDefaultSound) {
            val assetFileDescriptor = viewContext.assets.openFd("default/${alarm.soundFileName}")
            retriever.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
        } else {
            retriever.setDataSource(
                viewContext,
                Uri.parse(alarm.soundFileUri)
            )
        }
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val durationMilli = Integer.parseInt(duration)
        this.durationMilli = durationMilli
        this.viewContext = viewContext
        this.listAdapter = listAdapter
    }
}
