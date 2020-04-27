package net.banatech.app.android.sabi_alarm.sound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.ListFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.default_sound_view.*
import net.banatech.app.android.sabi_alarm.R

class SoundPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        return if (position == 0) {
            DefaultSoundPageFragment()
        } else {
            LocalSoundPageFragment()
        }
    }

    class DefaultSoundPageFragment : ListFragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            // Inflate the layout for this fragment
            val assetManager = this.resources.assets
            val defaultSoundDir = assetManager.list("default")
            default_sound_list.layoutManager = LinearLayoutManager(this.context)
            default_sound_list.adapter = DefaultSoundAdapter(defaultSoundDir)
            val dividerItemDecoration = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
            default_sound_list.addItemDecoration(dividerItemDecoration)
            default_sound_list.setHasFixedSize(true)
            return inflater.inflate(R.layout.default_sound_view, container, false)
        }
    }

    class LocalSoundPageFragment : ListFragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.local_sound_view, container, false)
        }
    }
}