package com.hazel.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hazel.myapplication.ui.fragments.tabs.DocsFragment
import com.hazel.myapplication.ui.fragments.tabs.ImagesFragment
import com.hazel.myapplication.ui.fragments.tabs.PdfFragment
import com.hazel.myapplication.ui.fragments.tabs.PptFragment
import com.hazel.myapplication.ui.fragments.tabs.SavedFragment
import com.hazel.myapplication.ui.fragments.tabs.VideosFragment
import com.hazel.myapplication.ui.fragments.tabs.XlsFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return list.size
    }

    private val list = listOf(
        ImagesFragment.newInstance(),
        VideosFragment.newInstance(),
        PdfFragment.newInstance(),
        DocsFragment.newInstance(),
        XlsFragment.newInstance(),
        PptFragment.newInstance(),
        SavedFragment.newInstance()
    )

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> list[0]
            1 -> list[1]
            2 -> list[2]
            3 -> list[3]
            4 -> list[4]
            5 -> list[5]
            6 -> list[6]
            else -> list[0]
        }
    }



    fun handleBackPress(position: Int): Boolean {
        return when (val fragment = list[position]) {
            is ImagesFragment -> {
                fragment.handleBackPress()
            }

            is VideosFragment -> {
                fragment.handleBackPress()
            }

            is PdfFragment -> {
                fragment.handleBackPress()
            }

            is DocsFragment -> {
                fragment.handleBackPress()
            }

            is XlsFragment -> {
                fragment.handleBackPress()
            }

            is PptFragment -> {
                fragment.handleBackPress()
            }

            is SavedFragment -> {
                fragment.handleBackPress()
            }

            else -> false
        }
    }

    fun handlePageChange(position: Int) {
        when (val fragment = list[position]) {
            is ImagesFragment -> {
                fragment.handlePageChange()
            }

            is VideosFragment -> {
                fragment.handlePageChange()
            }

            is PdfFragment -> {
                fragment.handlePageChange()
            }

            is DocsFragment -> {
                fragment.handlePageChange()
            }

            is XlsFragment -> {
                fragment.handlePageChange()
            }

            is PptFragment -> {
                fragment.handlePageChange()
            }

            is SavedFragment -> {
                fragment.handlePageChange()
            }
        }
    }
}