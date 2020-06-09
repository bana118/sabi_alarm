package net.banatech.app.android.sabi_alarm.sound

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.default_sound_view.*
import kotlinx.android.synthetic.main.default_sound_view.view.*
import kotlinx.android.synthetic.main.sound_pager_view.*
import kotlinx.android.synthetic.main.sound_select.*
import net.banatech.app.android.sabi_alarm.R

class SoundSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sound_select)
        setSupportActionBar(toolbar)
        sound_pager.adapter = SoundPagerAdapter(this)
        sound_pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        TabLayoutMediator(tab_layout, sound_pager) { tab, position ->
            require(position == 0 || position == 1) { "Tab position must be 0 or 1" }
            if (position == 0) {
                tab.text = "Default"
            } else if (position == 1) {
                tab.text = "Local"
            }
        }.attach()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}