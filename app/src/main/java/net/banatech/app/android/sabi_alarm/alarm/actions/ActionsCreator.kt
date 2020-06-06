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

//    fun toggleComplete(todo: Todo) {
//        val id: Long = todo.getId()
//        val actionType: String =
//            if (todo.isComplete()) TodoActions.TODO_UNDO_COMPLETE else TodoActions.TODO_COMPLETE
//        dispatcher.dispatch(
//            actionType,
//            TodoActions.KEY_ID, id
//        )
//    }
//
//    fun toggleCompleteAll() {
//        dispatcher.dispatch(TodoActions.TODO_TOGGLE_COMPLETE_ALL)
//    }
//
//    fun destroyCompleted() {
//        dispatcher.dispatch(TodoActions.TODO_DESTROY_COMPLETED)
//    }

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