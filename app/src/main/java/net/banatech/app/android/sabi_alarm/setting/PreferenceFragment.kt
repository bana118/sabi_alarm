package net.banatech.app.android.sabi_alarm.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import net.banatech.app.android.sabi_alarm.R

class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
