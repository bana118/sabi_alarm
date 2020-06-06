package net.banatech.app.android.sabi_alarm.alarm.stores

import net.banatech.app.android.sabi_alarm.alarm.actions.Action
import net.banatech.app.android.sabi_alarm.alarm.dispatcher.Dispatcher

abstract class Store(private val dispatcher: Dispatcher) {

    fun emitStoreChange() {
        dispatcher.emitChange(changeEvent())
    }

    abstract fun changeEvent(): StoreChangeEvent
    abstract fun onAction(action: Action)

    interface StoreChangeEvent
}