package net.banatech.app.android.sabi_alarm.stores.sound

import android.content.Context
import android.util.Log
import net.banatech.app.android.sabi_alarm.actions.Action
import net.banatech.app.android.sabi_alarm.actions.sound.SoundActions
import net.banatech.app.android.sabi_alarm.dispatcher.Dispatcher
import net.banatech.app.android.sabi_alarm.sound.database.Sound
import net.banatech.app.android.sabi_alarm.stores.Store
import org.greenrobot.eventbus.Subscribe

object SoundStore{
    val sounds: ArrayList<Sound> = ArrayList()

    @Subscribe
    @SuppressWarnings("unchecked")
    fun onAction(action: Action) {
        Log.d("onAction", "type${action.type}")
        when (action.type) {
            SoundActions.SOUND_ADD -> {
                val soundFileName = action.data[SoundActions.KEY_SOUND_FILE_NAME]
                val stringUri = action.data[SoundActions.KEY_SOUND_STRING_URI]
                val context = action.data[SoundActions.KEY_CONTEXT]
                check(soundFileName is String){ "SoundFileName value must be String"}
                check(stringUri is String){ "SoundStringUri value must be String"}
                check(context is Context) { "Context value must be Context" }
                add(soundFileName, stringUri, context)
                emitSoundStoreAdd()
            }
            SoundActions.SOUND_REMOVE -> {
                val id = action.data[SoundActions.KEY_ID]
                val context = action.data[SoundActions.KEY_CONTEXT]
                check(id is Int) { "Id value must be Int" }
                check(context is Context) { "Context value must be Context" }
                remove(id, context)
                emitSoundStoreRemove()
            }
        }
    }

    private fun add(soundFileName: String, stringUri: String, context: Context){
        val id = System.currentTimeMillis().toInt() //TODO unnecessary when using database
        val sound = Sound(
            id = id,
            fileName = soundFileName,
            stringUri = stringUri
        )
        sounds.add(sound)
    }

    private fun remove(id: Int, context: Context) {
        val sound = sounds.first { it.id == id }
        sounds.remove(sound)
    }

    fun emitSoundStoreAdd() {
        Dispatcher.emitEvent(addSoundEvent())
    }

    fun emitSoundStoreRemove() {
        Dispatcher.emitEvent(removeSoundEvent())
    }

    private fun addSoundEvent(): SoundStoreAddEvent {
        return SoundStoreAddEvent()
    }

    private fun removeSoundEvent(): SoundStoreRemoveEvent {
        return SoundStoreRemoveEvent()
    }

    class SoundStoreAddEvent: Store.StoreEvent
    class SoundStoreRemoveEvent: Store.StoreEvent
}