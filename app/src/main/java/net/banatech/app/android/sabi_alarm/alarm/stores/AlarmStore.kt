package net.banatech.app.android.sabi_alarm.alarm.stores

import net.banatech.app.android.sabi_alarm.alarm.actions.Action
import net.banatech.app.android.sabi_alarm.alarm.actions.AlarmActions
import net.banatech.app.android.sabi_alarm.alarm.dispatcher.Dispatcher
import net.banatech.app.android.sabi_alarm.database.Alarm
import org.greenrobot.eventbus.Subscribe
import kotlin.collections.ArrayList

class AlarmStore (dispatcher: Dispatcher): Store(dispatcher){
    val alarms: ArrayList<Alarm> = ArrayList()
    private lateinit var lastDeleted: Alarm
    var canUndo = false

    @Subscribe
    @SuppressWarnings("unchecked")
    override fun onAction(action: Action){
        when(action.type){
            AlarmActions.ALARM_CREATE -> {
                val hour = action.data[AlarmActions.KEY_HOUR]
                val minute = action.data[AlarmActions.KEY_MINUTE]
                check(hour is Int && minute is Int){"Hour and minute value must be Int"}
                create(hour, minute)
                emitStoreChange()
            }
            AlarmActions.ALARM_DESTROY -> {
                val id = action.data[AlarmActions.KEY_ID]
                check(id is Int){"Id value must be Int"}
                destroy(id)
                emitStoreChange()
            }
            AlarmActions.ALARM_UNDO_DESTROY -> {
                undoDestroy()
                emitStoreChange()
            }
        }
    }

    private fun create(hour: Int, minute: Int) {
        val timeText = String.format("%02d:%02d", hour, minute)
        val alarm = Alarm(
            hour = hour,
            minute = minute,
            timeText = timeText,
            isVibration = false,
            isRepeatable = false,
            isSundayAlarm = false,
            isMondayAlarm = true,
            isTuesdayAlarm = false,
            isWednesdayAlarm = true,
            isThursdayAlarm = true,
            isFridayAlarm = false,
            isSaturdayAlarm = true,
            soundFileName = "beethoven_no5_1st.mp3",
            soundStartTime = 0,
            isDefaultSound = true
        )
        addElement(alarm)
    }

    private fun destroy(id: Int) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if(alarm.id == id) {
                lastDeleted = alarm.copy()
                canUndo = true
                iter.remove()
                break
            }
        }
    }

    private fun getById(id: Int): Alarm? {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if(alarm.id == id) {
                return alarm
            }
        }
        return null
    }

    private fun undoDestroy() {
        if(canUndo){
            addElement(lastDeleted.copy())
            canUndo = false
        }
    }

    private fun addElement(clone: Alarm) {
        alarms.add(clone)
        alarms.sortWith(compareBy({it.hour}, {it.minute}))
    }

    override fun changeEvent(): StoreChangeEvent {
        return AlarmStoreChangeEvent()
    }

    class AlarmStoreChangeEvent: StoreChangeEvent

}