package com.BlackBloodE.WaTaShiWaNo1.ui.nhentai

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.BlackBloodE.WaTaShiWaNo1.R
import com.smarx.notchlib.NotchScreenManager
import kotlinx.android.synthetic.main.activity_manga.*
import kotlinx.android.synthetic.main.fragment_manga.view.*
import kotlinx.android.synthetic.main.popupview_item.view.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


var link : String = ""
var finP : Int = 1
var num : String = ""
var nowPages = 1
val metrics = DisplayMetrics()
private lateinit var mQuestionPopupWindow : PopupWindow
private lateinit var rootView : View

class Manga : AppCompatActivity() {

    private var intentFilter: IntentFilter? = null
    //private var timeChangeReceiver: TimeChangeReceiver? = null

    //電量偵測
    private val batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val lv = intent.getIntExtra("level", 0)
            tVBattery.text = ("$lv%")
        }
    }
    private val TimeChangeReceiver : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK -> {
                    tVTime.text = SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime())
                } //每过一分钟 触发
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manga)

        NotchScreenManager.getInstance().setDisplayInNotch(this)// 支持显示到刘海区域
        // 获取刘海屏信息
        NotchScreenManager.getInstance().getNotchInfo(
            this
        ) { notchScreenInfo ->
            if (notchScreenInfo.hasNotch) {
                for (rect in notchScreenInfo.notchRects) {
                    val layoutParams =
                        tVPage.getLayoutParams() as ConstraintLayout.LayoutParams
                    layoutParams.topMargin = rect.bottom
                    tVPage.setLayoutParams(layoutParams)
                }
            }
        }

        link = getIntent().getStringExtra("link")
        finP = getIntent().getIntExtra("finP",1)
        num = getIntent().getStringExtra("num")
        this.getWindowManager()?.getDefaultDisplay()?.getMetrics(metrics)

        //連接Adapter，讓畫面(Fragment)與ViewPager建立關聯
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                tVPage.text = (position+1).toString()+" / "+finP
                tVload.text = (position+1).toString()
                nowPages = position+1
            }
            override fun onPageSelected(position: Int) {}
        })
        tVPage.text = 1.toString()+" / "+finP
        tVTime.text = SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime())

        this.registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))//註冊電量偵測

        intentFilter = IntentFilter()
        intentFilter?.addAction(Intent.ACTION_TIME_TICK) //每分钟变化

        registerReceiver(TimeChangeReceiver, intentFilter)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 什麼都不用寫
        }
        else {
            // 什麼都不用寫
        }
    }
}

class ViewPagerAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fm) {
    //回傳對應位置的Fragment，決定頁面的呈現順序
    override fun getItem(position: Int) = when(position){
        position -> DetailsFragment(position+1)    //第position頁要呈現的Fragment
        else -> DetailsFragment(position+1)
    }
    //回傳Fragment頁數
    override fun getCount() = finP
}

class DetailsFragment(private val index:Int) : Fragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = LayoutInflater.from(context).inflate(R.layout.fragment_manga, container, false)
        link = link.replace("t.","i.")
        v.WV.settings.domStorageEnabled = true // 解决对某些标签的不支持出现白屏
        //將webview背景設成透明
        v.WV.setBackgroundColor(ContextCompat.getColor(context!!,android.R.color.transparent))
        v.WV.setBackgroundResource(android.R.color.transparent)
        Thread{
            var pagelink = "https://nhentai.net/g/"
            lateinit var html : Document
            try {
                html = Jsoup.connect(pagelink+num+"/"+index+"/").userAgent("Mozilla/5.0 Chrome/26.0.1410.64 Safari/537.31").get()
                pagelink = html?.select("a[href~=/g/] img[src]")?.attr("src").toString()
                getActivity()?.runOnUiThread(Runnable {
                    v.WV.loadUrl(pagelink)
                    v.WV.settings.useWideViewPort = true //将图片调整到适合webview的大小
                    v.WV.settings.loadWithOverviewMode = true // 缩放至屏幕的大小
                    v.WV.settings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
                    v.WV.settings.displayZoomControls = false //隐藏原生的缩放控件
                })
            }catch (e:Exception){
            }

        }.start()
        val VP: ViewPager = activity!!.findViewById(R.id.viewPager)
        var ll : GestureDetector.SimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener(){
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                //Toast.makeText(context,"123",Toast.LENGTH_SHORT).show()
                return super.onDoubleTap(e)
            }
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                activity?.getWindowManager()?.getDefaultDisplay()?.getMetrics(metrics)
                var MX = metrics.widthPixels
                var MY = metrics.heightPixels
                if (e != null) {
                    when(e.x){
                        in 0..metrics.widthPixels/2 -> {
                            if (nowPages==1){
                                println(nowPages)
                                Toast.makeText(activity,"已經是首頁啦!",Toast.LENGTH_SHORT).show()
                            }else{
                                println("上一頁")
                                VP.currentItem = nowPages-2
                                println(nowPages)
                            }
                        }
                        in metrics.widthPixels/2..metrics.widthPixels ->{
                            if (nowPages== finP){
                                println(nowPages)
                                Toast.makeText(activity,"已經是最後一頁啦!",Toast.LENGTH_SHORT).show()
                            }else{
                                println("下一頁")
                                VP.currentItem = nowPages
                                println(nowPages)
                            }
                        }
                    }
                }
                return false
            }
            override fun onLongPress(e: MotionEvent?) {
                println("長按")
                setVibrate(100)
                PopupView()
                super.onLongPress(e)
            }
        }
        var dd  = GestureDetector(context,ll)
        v.WV.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return dd.onTouchEvent(event)
            }
        })

        return v
    }

    fun PopupView(){
        //隱藏鍵盤(mActivity是你當前的activity)
        var mIBinder: IBinder? = null
        if (activity?.getCurrentFocus() != null) {
            mIBinder = activity?.getCurrentFocus()!!.getWindowToken()
        }

        val mInputMethodManager: InputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS)

        //創造PopupView(popupview_item是你要顯示在View上的xml)
        val nullParent: ViewGroup? = null
        val popupView: View = layoutInflater.inflate(R.layout.popupview_item, nullParent)

        //設定PopupView
        mQuestionPopupWindow = PopupWindow(
            popupView,
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        mQuestionPopupWindow.setTouchable(true)
        mQuestionPopupWindow.setOutsideTouchable(true)
        mQuestionPopupWindow.setBackgroundDrawable(BitmapDrawable(resources, null as Bitmap?))
        mQuestionPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        mQuestionPopupWindow.animationStyle = R.style.pop_animation
        //設定rootView 讓PopupView在rootView之上開啟
        rootView = getLayoutInflater().inflate(R.layout.fragment_manga, nullParent)
        mQuestionPopupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0)

        //設定透明度(popupview視窗外設為灰色)
        var lp : WindowManager.LayoutParams? = activity?.getWindow()?.getAttributes()
        lp?.alpha = 0.4f
        activity?.getWindow()?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        activity?.getWindow()?.setAttributes(lp)

        mQuestionPopupWindow.setOnDismissListener(PopupWindow.OnDismissListener() {
            // 在dismiss中恢复透明度
            var lp : WindowManager.LayoutParams? = activity?.getWindow()?.getAttributes()
            lp?.alpha = 1f
            activity?.getWindow()?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            activity?.getWindow()?.setAttributes(lp)
        })
        val VP: ViewPager = activity!!.findViewById(R.id.viewPager)
        popupView.seekBarPages.max = finP-1
        popupView.seekBarPages.progress = index-1
        popupView.seekBarPages.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            var nowPage = 1
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //VP.currentItem = progress//放棄不用的原因是因為當遇到頁數過多的本子時，一次滑動太多頁會被google當作爬蟲機器人封鎖
                nowPage = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                VP.currentItem = nowPage
            }
        })
        popupView.btnF5.setOnClickListener {
            var WV : WebView? = activity?.findViewById(R.id.WV)
            WV?.reload()
        }
    }

    fun setVibrate(time: Int) {
        val myVibrator : Vibrator = context?.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
            //getSystemService(Service.VIBRATOR_SERVICE)
        myVibrator!!.vibrate(time.toLong())
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
    override fun onPause() {
        super.onPause()
    }
}