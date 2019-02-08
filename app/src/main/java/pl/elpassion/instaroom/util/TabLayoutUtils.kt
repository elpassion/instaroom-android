package pl.elpassion.instaroom.util

import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout


object TabLayoutUtils {

    fun enableTab(tabLayout: TabLayout, tabPos: Int, enable: Boolean) {
        val viewGroup = getTabViewGroup(tabLayout)
        viewGroup?.run {
            val tabView = viewGroup.getChildAt(tabPos)
            tabView.isEnabled = enable
            tabView.alpha = if (enable) 1f else 0.3f
        }
    }

    private fun getTabViewGroup(tabLayout: TabLayout): ViewGroup? {
        var viewGroup: ViewGroup? = null
        if (tabLayout.childCount > 0) {
            val view = tabLayout.getChildAt(0)
            if (view is ViewGroup)
                viewGroup = view
        }
        return viewGroup
    }

}