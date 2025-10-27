package cn.plaso.liveclasssdkdemo.resourcecenter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import cn.plaso.liveclasssdkdemo.R

class CloudFilePickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_file_picker)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = TITLES.size
            override fun createFragment(position: Int) = CloudFileListFragment.newInstance(PREPARSED_FLAGS[position])
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = TITLES[position]
        }.attach()
    }

    companion object {
        private val TITLES = listOf("非预解析文件", "预解析文件")
        private val PREPARSED_FLAGS = listOf(false, true)
    }
}