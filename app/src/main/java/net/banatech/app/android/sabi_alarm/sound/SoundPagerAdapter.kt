package net.banatech.app.android.sabi_alarm.sound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.alarm_view.view.*
import kotlinx.android.synthetic.main.default_sound_view.view.*
import kotlinx.android.synthetic.main.sound_file_view.view.*
import net.banatech.app.android.sabi_alarm.R
import net.banatech.app.android.sabi_alarm.alarm.AlarmAdapter

class SoundPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        return if (position == 0) {
            DefaultSoundPageFragment()
        } else {
            LocalSoundPageFragment()
        }
    }

    class DefaultSoundPageFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return inflater.inflate(R.layout.default_sound_view, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            val soundList = view.default_sound_list
            val assetManager = this.resources.assets
            val defaultSoundDir = assetManager.list("default")
            check(defaultSoundDir != null){"default sound list must not be null"}
            soundList.layoutManager =  LinearLayoutManager(this.context)
            val defaultSoundAdapter = DefaultSoundAdapter(defaultSoundDir)
            soundList.adapter = defaultSoundAdapter
            val dividerItemDecoration = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
            soundList.addItemDecoration(dividerItemDecoration)
        }
    }

    class LocalSoundPageFragment : Fragment() {

    }
}