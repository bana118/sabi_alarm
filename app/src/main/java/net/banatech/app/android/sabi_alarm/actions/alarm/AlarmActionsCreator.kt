package net.banatech.app.android.sabi_alarm.actions.alarm

import android.content.Context
import net.banatech.app.android.sabi_alarm.actions.alarm.AlarmActions
import net.banatech.app.android.sabi_alarm.dispatcher.Dispatcher

object AlarmActionsCreator {

    fun create(hour: Int, minute: Int, context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_CREATE,
            AlarmActions.KEY_HOUR, hour,
            AlarmActions.KEY_MINUTE, minute,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun destroy(id: Int, context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_DESTROY,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun undoDestroy(context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_UNDO_DESTROY,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun edit(id: Int, hour: Int, minute: Int, context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_EDIT,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_HOUR, hour,
            AlarmActions.KEY_MINUTE, minute,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun switchEnable(id: Int, enable: Boolean, context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_ENABLE_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_ENABLE, enable,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun switchDetail(id: Int, isShowDetail: Boolean, context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_IS_SHOW_DETAIL_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_IS_SHOW_DETAIL_SWITCH, isShowDetail,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun switchVibration(id: Int, isVibration: Boolean, context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_IS_VIBRATION_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_IS_VIBRATION, isVibration,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun switchRepeatable(id: Int, isReadable: Boolean, context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_IS_REPEATABLE_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_IS_REPEATABLE, isReadable,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun switchDayAlarm(id: Int, dayOfWeek: Int, dayEnable: Boolean, context: Context) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_DAY_SWITCH,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_DAY_OF_WEEK, dayOfWeek,
            AlarmActions.KEY_DAY_ENABLE, dayEnable,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun selectSound(
        id: Int,
        soundFileName: String,
        isDefaultSound: Boolean,
        soundFileUri: String,
        context: Context
    ) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_SOUND_SELECT,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_SOUND_FILE_NAME, soundFileName,
            AlarmActions.KEY_IS_DEFAULT_SOUND, isDefaultSound,
            AlarmActions.KEY_SOUND_FILE_URI, soundFileUri,
            AlarmActions.KEY_CONTEXT, context
        )
    }

    fun changeSoundStartTime(
        id: Int,
        soundStartTime: Int,
        soundStartTimeText: String,
        context: Context
    ) {
        Dispatcher.dispatch(
            AlarmActions.ALARM_SOUND_START_TIME_CHANGE,
            AlarmActions.KEY_ID, id,
            AlarmActions.KEY_SOUND_START_TIME, soundStartTime,
            AlarmActions.KEY_SOUND_START_TIME_TEXT, soundStartTimeText,
            AlarmActions.KEY_CONTEXT, context
        )
    }
}
