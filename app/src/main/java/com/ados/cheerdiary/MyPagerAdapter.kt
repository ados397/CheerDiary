package com.ados.cheerdiary

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.cheerdiary.page.*

class MyPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val NUM_PAGES = 4

    override fun getItemCount(): Int  = NUM_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FragmentPageDashboard.newInstance("Page1", "")
            }
            1 -> {
                FragmentPageFanClub.newInstance("Page2", "")
            }
            2 -> {
                FragmentPageSchedule.newInstance("Page3", "")
            }
            else -> {
                FragmentSuccessCalendarLayout.newInstance("Page4", "")
            }
        }
    }
}