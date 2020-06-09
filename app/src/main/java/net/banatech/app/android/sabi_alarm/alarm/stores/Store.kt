package net.banatech.app.android.sabi_alarm.alarm.stores

import net.banatech.app.android.sabi_alarm.alarm.actions.Action
import net.banatech.app.android.sabi_alarm.alarm.dispatcher.Dispatcher


abstract class Store {

    fun emitStoreCreate() {
        Dispatcher.emitEvent(createEvent())
    }

    fun emitStoreTimeChange() {
        Dispatcher.emitEvent(timeChangeEvent())
    }

    fun emitStoreChange() {
        Dispatcher.emitEvent(changeEvent())
    }

    fun emitStoreDestroy() {
        Dispatcher.emitEvent(destroyEvent())
    }

    fun emitStoreSoundSelect() {
        Dispatcher.emitEvent(soundSelectEvent())
    }

    abstract fun onAction(action: Action)
    abstract fun createEvent(): StoreCreateEvent
    abstract fun timeChangeEvent(): StoreTimeChangeEvent
    abstract fun changeEvent(): StoreChangeEvent
    abstract fun destroyEvent(): StoreDestroyEvent
    abstract fun soundSelectEvent(): StoreSoundSelectEvent


    interface StoreEvent
    interface StoreCreateEvent : StoreEvent
    interface StoreTimeChangeEvent : StoreEvent
    interface StoreChangeEvent : StoreEvent
    interface StoreDestroyEvent : StoreEvent
    interface StoreSoundSelectEvent: StoreEvent
}
