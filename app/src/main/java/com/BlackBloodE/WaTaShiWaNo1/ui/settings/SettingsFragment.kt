package com.BlackBloodE.WaTaShiWaNo1.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
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
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.parser.Parser
import java.net.URL


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

    private lateinit var listView: ListView
    private lateinit var listAdapter: ListAdapter
    var str = arrayListOf<String>("問題回報","贊助我", "消除廣告" , "檢查更新", "版本號 : ")

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

        str[4] = str[4]+LocalVersion().getLocalVersion(context!!)

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
            //Toast.makeText(context, "點選第 " + (position + 1) + " 個 \n內容：" + str[position], Toast.LENGTH_SHORT).show()
            when (position){
                0 ->{
                    openWebside("https://forms.gle/5MXvyfaycNLNvq517")//開啟問題回報表單
                }
                1 ->{
                    openWebside("https://www.buymeacoffee.com/gbwOGMA")//開啟贊助我連結
                }
                2 ->{

                }
                3 ->{
                    Thread{
                        var netVer = getNewVer()
                        var strUp = "需要更新!!"
                        var strNUP = "無須更新"
                        var ADt = ""
                        if (LocalVersion().getLocalVersion(context!!)!=netVer){
                            ADt = strUp
                        }else{
                            ADt = strNUP
                        }
                        Looper.prepare()
                        AlertDialog.Builder(context)
                            .setMessage("目前版本:"+LocalVersion().getLocalVersion(context!!)+"\n"+"最新版本:"+netVer)
                            .setTitle("檢查更新..."+ADt)
                            .setPositiveButton("取得最新版本", DialogInterface.OnClickListener { _, _ ->
                                openWebside("https://github.com/BlackBloodE/WaTaShiWaNo1/releases/latest")
                            })
                            .setNeutralButton("取消", null)
                            .create()
                            .show()
                        Looper.loop()
                    }.start()
                }
                4 ->{
                    //Toast.makeText(context, "您點選了 " + str[position] + " 但此功能尚未實現，不好意思", Toast.LENGTH_SHORT).show()
                }
            }
        }

    //解析GitHub Release的json
    fun getNewVer() : String {
        var netVer = ""

        val result = URL("https://api.github.com/repos/BlackBloodE/WaTaShiWaNo1/releases/latest").readText()
        val json: JSONObject = parse(result) as JSONObject
        netVer = json.getString("tag_name")
        //println(netVer)

        return netVer
    }
    fun parse(json: String): JSONObject? {
        var jsonObject: JSONObject? = null
        try {
            jsonObject = JSONObject(json)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }
    //開啟外部連結用
    fun openWebside(url: String){
        val fileName = Uri.parse(url)
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        intent.setData(fileName)
        startActivity(intent)
    }
}
class LocalVersion {
    //獲取版本號
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
