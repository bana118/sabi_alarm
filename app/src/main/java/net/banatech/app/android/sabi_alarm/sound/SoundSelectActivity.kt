package net.banatech.app.android.sabi_alarm.sound

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.default_sound_view.*
import kotlinx.android.synthetic.main.default_sound_view.view.*
import kotlinx.android.synthetic.main.sound_pager_view.*
import kotlinx.android.synthetic.main.sound_select.*
import net.banatech.app.android.sabi_alarm.R

class SoundSelectActivity : AppCompatActivity(){
    private val defaultSoundFileList = listOf(
        R.raw.beethoven_no5_1st,
        R.raw.dvorak_no9_4th,
        R.raw.mozart_einekleine,
        R.raw.pachelbel_canon,
        R.raw.schubert_unfinished
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sound_select)
        setSupportActionBar(toolbar)
        sound_pager.adapter = SoundPagerAdapter(this)
        sound_pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}