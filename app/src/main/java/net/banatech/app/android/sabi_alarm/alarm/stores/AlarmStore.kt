package net.banatech.app.android.sabi_alarm.alarm.stores

import net.banatech.app.android.sabi_alarm.alarm.actions.Action
import net.banatech.app.android.sabi_alarm.alarm.actions.AlarmActions
import net.banatech.app.android.sabi_alarm.alarm.dispatcher.Dispatcher
import net.banatech.app.android.sabi_alarm.database.Alarm
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.collections.ArrayList

class AlarmStore(dispatcher: Dispatcher) : Store(dispatcher) {
    companion object {
        val alarms: ArrayList<Alarm> = ArrayList()
        private lateinit var lastDeleted: Alarm
        var canUndo = false
    }

    @Subscribe
    @SuppressWarnings("unchecked")
    override fun onAction(action: Action) {
        when (action.type) {
            AlarmActions.ALARM_CREATE -> {
                val hour = action.data[AlarmActions.KEY_HOUR]
                val minute = action.data[AlarmActions.KEY_MINUTE]
                check(hour is Int && minute is Int) { "Hour and minute value must be Int" }
                create(hour, minute)
                emitStoreCreate()
            }
            AlarmActions.ALARM_DESTROY -> {
                val id = action.data[AlarmActions.KEY_ID]
                check(id is Int) { "Id value must be Int" }
                destroy(id)
                emitStoreDestroy()
            }
            AlarmActions.ALARM_UNDO_DESTROY -> {
                undoDestroy()
                emitStoreChange()
            }
            AlarmActions.ALARM_EDIT -> {
                val id = action.data[AlarmActions.KEY_ID]
                val hour = action.data[AlarmActions.KEY_HOUR]
                val minute = action.data[AlarmActions.KEY_MINUTE]
                check(id is Int) { "Id value must be Int" }
                check(hour is Int && minute is Int) { "Hour and minute value must be Int" }
                edit(id, hour, minute)
                emitStoreTimeChange()
            }
            AlarmActions.ALARM_ENABLE_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val enable = action.data[AlarmActions.KEY_ENABLE]
                check(id is Int) { "Id value must be Int" }
                check(enable is Boolean) { "Enable value must be Int" }
                switchEnable(id, enable)
                emitStoreChange()
            }
            AlarmActions.ALARM_IS_SHOW_DETAIL_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val isShowDetail = action.data[AlarmActions.KEY_IS_SHOW_DETAIL_SWITCH]
                check(id is Int) { "Id value must be Int" }
                check(isShowDetail is Boolean) { "IsShowDetail value must be Int" }
                switchDetail(id, isShowDetail)
                emitStoreChange()
            }
            AlarmActions.ALARM_IS_VIBRATION_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val isVibration = action.data[AlarmActions.KEY_IS_VIBRATION]
                check(id is Int) { "Id value must be Int" }
                check(isVibration is Boolean) { "IsVibration value must be Int" }
                switchVibration(id, isVibration)
                emitStoreChange()
            }
            AlarmActions.ALARM_IS_REPEATABLE_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val isReadable = action.data[AlarmActions.KEY_IS_REPEATABLE]
                check(id is Int) { "Id value must be Int" }
                check(isReadable is Boolean) { "IsRepeatable value must be Int" }
                switchRepeatable(id, isReadable)
                emitStoreChange()
            }
            AlarmActions.ALARM_DAY_SWITCH -> {
                val id = action.data[AlarmActions.KEY_ID]
                val dayOfWeek = action.data[AlarmActions.KEY_DAY_OF_WEEK]
                val dayEnable = action.data[AlarmActions.KEY_DAY_ENABLE]
                check(id is Int) { "Id value must be Int" }
                check(dayOfWeek is Int) { "DayOfWeek value must be Int" }
                check(dayEnable is Boolean) { "DayEnable value must be Boolean" }
                switchWeekEnable(id, dayOfWeek, dayEnable)
                emitStoreChange()
            }
        }
    }

    private fun create(hour: Int, minute: Int) {
        val id = System.currentTimeMillis().toInt() //TODO unnecessary when using database
        val timeText = String.format("%02d:%02d", hour, minute)
        val alarm = Alarm(
            id = id,
            hour = hour,
            minute = minute,
            timeText = timeText,
            enable = true,
            isShowDetail = false,
            isVibration = false,
            isRepeatable = false,
            isSundayAlarm = false,
            isMondayAlarm = true,
            isTuesdayAlarm = true,
            isWednesdayAlarm = true,
            isThursdayAlarm = true,
            isFridayAlarm = true,
            isSaturdayAlarm = false,
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
            if (alarm.id == id) {
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
            if (alarm.id == id) {
                return alarm
            }
        }
        return null
    }

    private fun undoDestroy() {
        if (canUndo) {
            addElement(lastDeleted.copy())
            canUndo = false
        }
    }

    private fun edit(id: Int, hour: Int, minute: Int) {
        val iter = alarms.iterator()
        val timeText = String.format("%02d:%02d", hour, minute)
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.hour = hour
                alarm.minute = minute
                alarm.timeText = timeText
                break
            }
        }
    }

    private fun switchEnable(id: Int, enable: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.enable = enable
                break
            }
        }
    }

    private fun switchDetail(id: Int, isShowDetail: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.isShowDetail = isShowDetail
                break
            }
        }
    }

    private fun switchVibration(id: Int, isVibration: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.isVibration = isVibration
                break
            }
        }
    }

    private fun switchRepeatable(id: Int, isReadable: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                alarm.isRepeatable = isReadable
                break
            }
        }
    }

    private fun switchWeekEnable(id: Int, dayOfWeek: Int, dayEnable: Boolean) {
        val iter = alarms.iterator()
        while (iter.hasNext()) {
            val alarm = iter.next()
            if (alarm.id == id) {
                when (dayOfWeek) {
                    Calendar.SUNDAY -> {
                        alarm.isSundayAlarm = dayEnable
                    }
                    Calendar.MONDAY -> {
                        alarm.isMondayAlarm = dayEnable
                    }
                    Calendar.TUESDAY -> {
                        alarm.isTuesdayAlarm = dayEnable
                    }
                    Calendar.WEDNESDAY -> {
                        alarm.isWednesdayAlarm = dayEnable
                    }
                    Calendar.THURSDAY -> {
                        alarm.isThursdayAlarm = dayEnable
                    }
                    Calendar.FRIDAY -> {
                        alarm.isFridayAlarm = dayEnable
                    }
                    Calendar.SATURDAY -> {
                        alarm.isSaturdayAlarm = dayEnable
                    }
                }
                break
            }
        }
    }

    private fun addElement(clone: Alarm) {
        alarms.add(clone)
    }

    override fun createEvent(): StoreCreateEvent {
        return AlarmStoreCreateEvent()
    }

    override fun timeChangeEvent(): StoreTimeChangeEvent {
        return AlarmStoreTimeChangeEvent()
    }

    override fun changeEvent(): StoreChangeEvent {
        return AlarmStoreChangeEvent()
    }

    override fun destroyEvent(): StoreDestroyEvent {
        return AlarmStoreDestroyEvent()
    }

    class AlarmStoreCreateEvent : StoreCreateEvent
    class AlarmStoreTimeChangeEvent : StoreTimeChangeEvent
    class AlarmStoreChangeEvent : StoreChangeEvent
    class AlarmStoreDestroyEvent : StoreDestroyEvent
}
