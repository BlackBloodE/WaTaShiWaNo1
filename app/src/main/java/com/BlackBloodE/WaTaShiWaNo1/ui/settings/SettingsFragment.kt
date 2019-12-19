package com.BlackBloodE.WaTaShiWaNo1.ui.settings

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.BlackBloodE.WaTaShiWaNo1.R


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

    private lateinit var listView: ListView
    private lateinit var listAdapter: ListAdapter
    var str = arrayListOf<String>("回報問題","斗內", "檢查更新", "版本號 : v")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
//        val textView: TextView = root.findViewById(R.id.text_tools)
//        settingsViewModel.text.observe(this, Observer {
//            textView.text = it
//        })

        str[3] = str[3]+getLocalVersion(context!!).toString()

        listView = root.findViewById(R.id.listView)
        listAdapter = ArrayAdapter<String>(
            context!!,
            R.layout.simple_list_item_1,
            str
        )
        listView.setAdapter(listAdapter)
        listView.setOnItemClickListener(onClickListView)

        return root
    }

    /***
     * 點擊ListView事件Method
     */
    private val onClickListView =
        OnItemClickListener { parent, view, position, id ->
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            Toast.makeText(
                context,
                "點選第 " + (position + 1) + " 個 \n內容：" + str[position],
                Toast.LENGTH_SHORT
            ).show()
        }
    /**
     * 获取本地软件版本名
     */
    fun getLocalVersion(ctx: Context): String {
        var localVersion = ""
        try {
            val packageInfo: PackageInfo = ctx.getApplicationContext()
                .getPackageManager()
                .getPackageInfo(ctx.getPackageName(), 0)
            localVersion = packageInfo.versionName
            //LogUtil.d("本软件的版本号：$localVersion")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return localVersion
    }
}