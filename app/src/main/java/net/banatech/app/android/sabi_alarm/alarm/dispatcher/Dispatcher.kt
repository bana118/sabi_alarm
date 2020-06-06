package net.banatech.app.android.sabi_alarm.alarm.dispatcher

import net.banatech.app.android.sabi_alarm.alarm.actions.Action
import net.banatech.app.android.sabi_alarm.alarm.stores.Store
import org.greenrobot.eventbus.EventBus

object Dispatcher {
    private val bus: EventBus = EventBus()

    fun register(cls: Any) {
        bus.register(cls)
    }

    fun unregister(cls: Any) {
        bus.unregister(cls)
    }

    fun emitChange(o: Store.StoreChangeEvent) {
        post(o)
    }

    fun dispatch(type: String, vararg data: Any) {
        require(type.isNotEmpty()){ "Type must not be empty" }
        require(data.size % 2 == 0){"Data must be a valid list of key,value pairs"}

        val actionBuilder = Action.type(type)
        for(i in data.indices step 2){
            val key = data[i].toString()
            val value = data[i+1]
            actionBuilder.bundle(key, value)
        }
        post(actionBuilder.build())
    }

    private fun post(event: Any){
        bus.post(event)
    }


}