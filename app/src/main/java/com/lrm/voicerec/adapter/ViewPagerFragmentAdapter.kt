package com.lrm.voicerec.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lrm.voicerec.fragments.RecordVoiceFragment
import com.lrm.voicerec.fragments.RecordingsFragment

class ViewPagerFragmentAdapter(
    fragment: Fragment
): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RecordVoiceFragment()
            1 -> RecordingsFragment()
            else -> RecordVoiceFragment()
        }
    }
}