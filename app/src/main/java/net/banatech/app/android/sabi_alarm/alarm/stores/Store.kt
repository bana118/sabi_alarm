package net.banatech.app.android.sabi_alarm.alarm.stores

import net.banatech.app.android.sabi_alarm.alarm.actions.Action
import net.banatech.app.android.sabi_alarm.alarm.dispatcher.Dispatcher

abstract class Store(private val dispatcher: Dispatcher) {

    fun emitStoreCreate() {
        dispatcher.emitEvent(createEvent())
    }

    fun emitStoreTimeChange() {
        dispatcher.emitEvent(timeChangeEvent())
    }

    fun emitStoreChange() {
        dispatcher.emitEvent(changeEvent())
    }

    fun emitStoreDestroy() {
        dispatcher.emitEvent(destroyEvent())
    }

    abstract fun createEvent(): StoreCreateEvent
    abstract fun timeChangeEvent(): StoreTimeChangeEvent
    abstract fun changeEvent(): StoreChangeEvent
    abstract fun destroyEvent(): StoreDestroyEvent
    abstract fun onAction(action: Action)

    interface StoreEvent
    interface StoreCreateEvent : StoreEvent
    interface StoreTimeChangeEvent : StoreEvent
    interface StoreChangeEvent : StoreEvent
    interface StoreDestroyEvent : StoreEvent
}
