package com.ados.cheerdiary

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.cheerdiary.page.*

class MyPagerAdapterFanClub(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val NUM_PAGES = 3

    override fun getItemCount(): Int  = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FragmentFanClubInfo.newInstance("Page1", "")
            }
            1 -> {
                FragmentFanClubRank.newInstance("Page2", "")
            }
            else -> {
                FragmentFanClubManagement.newInstance("Page4", "")
            }
        }
    }
}