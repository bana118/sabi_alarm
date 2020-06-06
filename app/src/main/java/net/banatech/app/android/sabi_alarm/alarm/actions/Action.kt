package net.banatech.app.android.sabi_alarm.alarm.actions

class Action(val type: String, val data: HashMap<String, Any>) {
    companion object{
        fun type(type: String): Builder {
            return Builder.with(type)
        }
    }

    object Builder {
        private lateinit var type: String
        private lateinit var data: HashMap<String, Any>

        fun with(type: String):Builder {
            this.type = type
            this.data = HashMap()
            return this
        }

        fun bundle(key: String, value: Any): Builder {
            data[key] = value
            return this
        }

        fun build(): Action {
            check(type.isNotEmpty()){
                "At least one key is required."
            }
            return Action(type, data)
        }
    }
}