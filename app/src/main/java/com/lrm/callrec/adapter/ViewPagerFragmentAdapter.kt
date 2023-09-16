package com.lrm.callrec.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lrm.callrec.fragments.RecordVoiceFragment
import com.lrm.callrec.fragments.RecordingsFragment

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