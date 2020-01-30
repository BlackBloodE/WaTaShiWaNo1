package com.BlackBloodE.WaTaShiWaNo1.ui.nhentai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.BlackBloodE.WaTaShiWaNo1.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.fragment_nhentai.*
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


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
    private var inFo = ""
    private var link : String = ""
    private var CoverLink = ""
    private lateinit var html : Document
    private var intPages : Int = 0
    var num = ""

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
}