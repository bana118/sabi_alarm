package net.banatech.app.android.sabi_alarm.alarm.actions

import net.banatech.app.android.sabi_alarm.alarm.dispatcher.Dispatcher


class ActionsCreator(private val dispatcher: Dispatcher) {

    fun create(hour: Int, minute: Int) {
        dispatcher.dispatch(
            AlarmActions.ALARM_CREATE,
            AlarmActions.KEY_HOUR, hour,
            AlarmActions.KEY_MINUTE, minute
        )
    }

    fun destroy(id: Int) {
        dispatcher.dispatch(
            AlarmActions.ALARM_DESTROY,
            AlarmActions.KEY_ID, id
        )
    }

    fun undoDestroy() {
        dispatcher.dispatch(
            AlarmActions.ALARM_UNDO_DESTROY
        )
    }

    fun edit(id: Int, hour: Int, minute: Int) {
        dispatcher.dispatch(
            AlarmActions.ALARM_EDIT,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_HOUR, hour,
            AlarmActions.KEY_MINUTE, minute
        )
    }

    fun switchEnable(id: Int, enable: Boolean) {
        dispatcher.dispatch(
            AlarmActions.ALARM_ENABLE_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_ENABLE, enable
        )
    }

    fun switchDetail(id: Int, isShowDetail: Boolean) {
        dispatcher.dispatch(
            AlarmActions.ALARM_IS_SHOW_DETAIL_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_IS_SHOW_DETAIL_SWITCH, isShowDetail
        )
    }

    fun switchVibration(id: Int, isVibration: Boolean) {
        dispatcher.dispatch(
            AlarmActions.ALARM_IS_VIBRATION_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_IS_VIBRATION, isVibration
        )
    }

    fun switchRepeatable(id: Int, isReadable: Boolean) {
        dispatcher.dispatch(
            AlarmActions.ALARM_IS_REPEATABLE_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_IS_REPEATABLE, isReadable
        )
    }

    fun switchDayAlarm(id: Int, dayOfWeek: Int, dayEnable: Boolean) {
        dispatcher.dispatch(
            AlarmActions.ALARM_DAY_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_DAY_OF_WEEK, dayOfWeek,
            AlarmActions.KEY_DAY_ENABLE, dayEnable
        )
    }

    fun seletSound(id: Int, soundFileName: String) {
        dispatcher.dispatch(
            AlarmActions.ALARM_SOUND_SELECT,
            AlarmActions.KEY_ID,  id,
            AlarmActions.KEY_SOUND_FILE_NAME, soundFileName
        )
    }

    companion object {
        private var instance: ActionsCreator? = null
        operator fun get(dispatcher: Dispatcher): ActionsCreator? {
            if (instance == null) {
                instance = ActionsCreator(dispatcher)
            }
            return instance
        }
    }
}
