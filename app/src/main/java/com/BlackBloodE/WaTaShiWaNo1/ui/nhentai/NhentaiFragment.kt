package com.BlackBloodE.WaTaShiWaNo1.ui.nhentai

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.BlackBloodE.WaTaShiWaNo1.*
import com.BlackBloodE.WaTaShiWaNo1.KeyboardChangeListener.KeyboardListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.fragment_nhentai.*
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*


class NhentaiFragment : Fragment(), View.OnClickListener {

    private lateinit var nhentaiViewModel: NhentaiViewModel
    private lateinit var editText3 : EditText
    private lateinit var buttonEnter : Button
    private lateinit var btnOutOpen : Button
    private lateinit var btnOpen : Button
    private lateinit var webview : WebView
    private lateinit var textViewTitlePsge : TextView
    private lateinit var textViewInfo : TextView
    private lateinit var ll : LinearLayout
    private lateinit var adView : AdView
    private var sv: ScrollView? = null
    private var inFo = ""
    private var link : String = ""
    private var CoverLink = ""
    private lateinit var html : Document
    private var intPages : Int = 0
    //歷史紀錄用
    private var listView: MyListView? = null
    private var helper: RecordSQLiteOpenHelper? = null
    private var db: SQLiteDatabase? = null
    private var adapter: BaseAdapter? = null
    private var tv_tip: TextView? = null
    private var tv_clear: TextView? = null

    var num = ""

    //建立共用變數類別
    val gv = GlobalVariable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nhentaiViewModel =
            ViewModelProviders.of(this).get(NhentaiViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_nhentai, container, false)
        editText3 = root.findViewById(R.id.editText3)
        buttonEnter = root.findViewById(R.id.buttonEnter)
        buttonEnter.setOnClickListener(this)
        btnOutOpen = root.findViewById(R.id.btnOutOpen)
        btnOutOpen.setOnClickListener(this)
        btnOpen = root.findViewById(R.id.btnOpen)
        btnOpen.setOnClickListener(this)
        webview = root.findViewById(R.id.webview)
        webview.settings.useWideViewPort = true
        webview.settings.loadWithOverviewMode = true
        //將webview背景設成透明
        webview.setBackgroundColor(ContextCompat.getColor(context!!,android.R.color.transparent))
        webview.setBackgroundResource(android.R.color.transparent)
        textViewTitlePsge = root.findViewById(R.id.textViewTitlePsge)
        textViewInfo = root.findViewById(R.id.textViewInfo)
        ll = root.findViewById(R.id.ll)
        // AdMob 初始化
        MobileAds.initialize(context, getString(R.string.admob_app_id))
        // 橫幅廣告
        adView = root.findViewById(R.id.adView)
        adView.loadAd(AdRequest.Builder().build())
        adView.isVisible = gv.getAdSwitch()
        sv = root.findViewById<View>(R.id.sv) as ScrollView
        tv_tip = root.findViewById<View>(R.id.tv_tip) as TextView
        listView = root.findViewById<View>(R.id.listView) as MyListView
        tv_clear = root.findViewById<View>(R.id.tv_clear) as TextView
        helper = RecordSQLiteOpenHelper(activity)
        KeyboardChangeListener.create(getActivity()!!)
            .setKeyboardListener(object : KeyboardListener {
                override fun onKeyboardChange(
                    isShow: Boolean,
                    keyboardHeight: Int
                ) {
                    if (isShow){
                        sv!!.visibility = View.VISIBLE
                    }else{
                        sv!!.visibility = View.INVISIBLE
                    }
                }
            })
        // 清空搜索历史
        tv_clear!!.setOnClickListener {
            deleteData()
            queryData("")
        }
        // 搜索框的文本变化实时监听
        editText3!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().trim { it <= ' ' }.length == 0) {
                    tv_tip!!.text = "搜尋紀錄"
                } else {
                    tv_tip!!.text = "搜索结果"
                }
                val tempName = editText3!!.text.toString()
                // 根据tempName去模糊查询数据库中有没有数据
                queryData(tempName)
            }
        })
        listView!!.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val textView =
                view.findViewById<View>(android.R.id.text1) as TextView
            val name = textView.text.toString()
            editText3!!.setText(name)
            Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
            // TODO 获取到item上面的文字，根据该关键字跳转到另一个页面查询，由你自己去实现
        })
        // 插入数据，便于测试，否则第一次进入没有数据怎么测试呀？
        val date = Date()
        val time = date.time
        insertData("Leo$time")
        // 第一次进入查询所有的历史记录
        queryData("")
        return root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonEnter ->{
                ll.removeAllViews()
                if (editText3.text.toString().equals("")){
                    textViewInfo.text = ""
                    inFo = ""
                    LinearLayout_infos.setVisibility(View.INVISIBLE)
                    LinearLayout_Buttons.setVisibility(View.INVISIBLE)
                    Toast.makeText(context,"請輸入神的語言",
                        Toast.LENGTH_SHORT).show()
                }else{
                    // 先隐藏键盘
                    (context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                        v.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                    // 按完搜索键后将当前查询的关键字保存起来,如果该关键字已经存在就不执行保存
                    val hasData =
                        hasData(editText3!!.text.toString().trim { it <= ' ' })
                    if (!hasData) {
                        insertData(editText3!!.text.toString().trim { it <= ' ' })
                        queryData("")
                    }
                    textViewInfo.text = ""
                    inFo = ""
                    link = "https://nhentai.net/g/"+editText3.text
                    num = editText3.text.toString()
                    progressBar.visibility = View.VISIBLE
                    Thread{
                        try {
                            html = Jsoup.connect(link).get()
                            var title = html?.select("div#info h2")
                            var pages = html?.select("div#info div")?.get(7)?.text().toString()
                            var parodiesLink = html?.select("span.tags a[href*=/parody/]")
                            var charactersLink = html?.select("span.tags a[href*=/character/]")
                            var tagsLink = html?.select("span.tags a[href*=/tag/]")
                            var artistsLink = html?.select("span.tags a[href*=/artist/]")
                            var groupsLink = html?.select("span.tags a[href*=/group/]")
                            var languagesLink = html?.select("span.tags a[href*=/Language/]")
                            var categoriesLink = html?.select("span.tags a[href*=/category/]")
                            intPages = pages.replace(" pages","").toInt()
                            CoverLink = html?.select("#cover img[src\$=.jpg]")?.attr("src").toString()
                            if (CoverLink.equals("")){
                                CoverLink = html?.select("#cover img[src\$=.png]")?.attr("src").toString()
                            }
                            println(CoverLink)
                            getActivity()?.runOnUiThread(Runnable {
                                webview.loadUrl(CoverLink)
                                var wvP  = arrayOfNulls<WebView>(intPages)
                                for (i in 0..intPages-1){
                                    var nP = ""
                                    nP = html?.select("a.gallerythumb[href~=/g/"+num+"/"+(i+1)+"] img[src]").attr("data-src").toString()
                                    println("np:"+nP)
                                    wvP[i] = WebView(context)
                                    wvP[i]?.loadUrl(nP)
                                    wvP[i]?.settings?.useWideViewPort = true //将图片调整到适合webview的大小
                                    wvP[i]?.settings?.loadWithOverviewMode = true //缩放至屏幕的大小
                                    val params =
                                        LinearLayout.LayoutParams(285, 390)
                                    params.setMargins(20, 0, 20, 0)
                                    wvP[i]?.layoutParams = params
                                    ll.addView(wvP[i])
                                }
                                textViewTitlePsge.text = title?.text()+"\n"+pages
                                if (!parodiesLink?.text().equals("")){
                                    inFo = "Parodies : "+parodiesLink?.text().toString()+"\n"
                                }
                                if (!charactersLink?.text().equals("")){
                                    inFo = inFo+"Characters : "+charactersLink?.text().toString()+"\n"
                                }
                                if (!tagsLink?.text().equals("")){
                                    inFo = inFo+"Tags : "+tagsLink?.text().toString()+"\n"
                                }
                                if (!artistsLink?.text().equals("")){
                                    inFo = inFo+"Artists : "+artistsLink?.text().toString()+"\n"
                                }
                                if (!groupsLink?.text().equals("")){
                                    inFo = inFo+"Groups : "+groupsLink?.text().toString()+"\n"
                                }
                                if (!languagesLink?.text().equals("")){
                                    inFo = inFo+"Languages : "+languagesLink?.text().toString()+"\n"
                                }
                                if (!categoriesLink?.text().equals("")){
                                    inFo = inFo+"Categories : "+categoriesLink?.text().toString()
                                }
                                inFo.replace("\n", "")
                                textViewInfo.text = inFo
                                LinearLayout_infos.setVisibility(View.VISIBLE)
                                LinearLayout_Buttons.setVisibility(View.VISIBLE)
                                progressBar.visibility = View.GONE
                            })
                        }catch (e: HttpStatusException){
                            getActivity()?.runOnUiThread(Runnable {
                                progressBar.visibility = View.GONE
                                MyToast()
                            })
                        }
                    }.start()
                }
            }
            R.id.btnOutOpen ->{
                val fileName = Uri.parse(link)
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Intent.ACTION_VIEW
                intent.setData(fileName)
                startActivity(intent)
            }
            R.id.btnOpen ->{
                //初始化Intent物件
                val intent = Intent()
                intent.putExtra("link" , CoverLink)
                intent.putExtra("finP" , intPages)
                intent.putExtra("num" , num)
                //從MainActivity 到Main2Activity
                context?.let { intent.setClass(it, Manga::class.java) }
                //動畫
                //val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, webview,"cover")
                //開啟Activity
                startActivity(intent)
            }
        }
    }

    fun MyToast(){
        Toast.makeText(context,"神的語言有誤",
            Toast.LENGTH_SHORT).show()
    }

    /**
     * 插入数据
     */
    private fun insertData(tempName: String) {
        db = helper!!.getWritableDatabase()
        db!!.execSQL("insert into records(name) values('$tempName')")
        db!!.close()
    }

    /**
     * 模糊查询数据
     */
    private fun queryData(tempName: String) {
        val cursor: Cursor = helper!!.getReadableDatabase().rawQuery(
            "select id as _id,name from records where name like '%$tempName%' order by id desc ",
            null
        )
        // 创建adapter适配器对象
        adapter = SimpleCursorAdapter(
            context,
            android.R.layout.simple_list_item_1,
            cursor,
            arrayOf("name"),
            intArrayOf(android.R.id.text1),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        // 设置适配器
        listView!!.setAdapter(adapter)
        (adapter as SimpleCursorAdapter).notifyDataSetChanged()
    }

    /**
     * 检查数据库中是否已经有该条记录
     */
    private fun hasData(tempName: String): Boolean {
        val cursor: Cursor = helper!!.getReadableDatabase().rawQuery(
            "select id as _id,name from records where name =?", arrayOf(tempName)
        )
        //判断是否有下一个
        return cursor.moveToNext()
    }

    /**
     * 清空数据
     */
    private fun deleteData() {
        db = helper!!.getWritableDatabase()
        db!!.execSQL("delete from records")
        db!!.close()
    }
}