package net.banatech.app.android.sabi_alarm

data class Alarm(val createdAt: Long, var hour: Int, var minute: Int, var timeText: String, var isVibration: Boolean,
                 var isRepeatable: Boolean, val weekAlarmList: BooleanArray){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Alarm

        if (createdAt != other.createdAt) return false
        if (hour != other.hour) return false
        if (minute != other.minute) return false
        if (timeText != other.timeText) return false
        if (isVibration != other.isVibration) return false
        if (isRepeatable != other.isRepeatable) return false
        if (!weekAlarmList.contentEquals(other.weekAlarmList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createdAt.hashCode()
        result = 31 * result + hour
        result = 31 * result + minute
        result = 31 * result + timeText.hashCode()
        result = 31 * result + isVibration.hashCode()
        result = 31 * result + isRepeatable.hashCode()
        result = 31 * result + weekAlarmList.contentHashCode()
        return result
    } // createdAt = yyyyMMddHHssSSS
}
