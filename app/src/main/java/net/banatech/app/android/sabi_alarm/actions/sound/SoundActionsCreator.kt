package net.banatech.app.android.sabi_alarm.actions.sound

import android.content.Context
import net.banatech.app.android.sabi_alarm.dispatcher.Dispatcher

object SoundActionsCreator {

    fun add(soundFileName: String, stringUri: String, context: Context) {
        Dispatcher.dispatch(
            SoundActions.SOUND_ADD,
            SoundActions.KEY_SOUND_FILE_NAME, soundFileName,
            SoundActions.KEY_SOUND_STRING_URI, stringUri,
            SoundActions.KEY_CONTEXT, context
        )
    }

    fun remove(id: Int, context: Context) {
        Dispatcher.dispatch(
            SoundActions.SOUND_REMOVE,
            SoundActions.KEY_ID, id,
            SoundActions.KEY_CONTEXT, context
        )
    }
}
