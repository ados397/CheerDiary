package com.ados.cheerdiary

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ados.cheerdiary.page.FragmentPageDashboard
import com.ados.cheerdiary.page.FragmentPageFanClub
import com.ados.cheerdiary.page.FragmentPageSchedule
import com.ados.cheerdiary.page.FragmentSuccessCalendar

class MyPagerAdapterSuccessCalendar(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val pageCount = Int.MAX_VALUE
    val firstFragmentPosition = Int.MAX_VALUE / 2

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        val calendarFragment = FragmentSuccessCalendar()
        calendarFragment.pageIndex = position
        return calendarFragment
    }
}