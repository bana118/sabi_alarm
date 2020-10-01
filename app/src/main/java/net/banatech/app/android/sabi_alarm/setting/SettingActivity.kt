package net.banatech.app.android.sabi_alarm.setting

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.setting.*
import net.banatech.app.android.sabi_alarm.BuildConfig
import net.banatech.app.android.sabi_alarm.R

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting)
        setSupportActionBar(toolbar)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, PreferenceFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        MobileAds.initialize(this) {}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (setting_ad_view_layout.childCount == 0) {
            val adView = AdView(this)
            adView.adSize = AdSize.BANNER
            adView.adUnitId = BuildConfig.ADMOB_BANNER_ID
            setting_ad_view_layout.addView(adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }
}
