package com.BlackBloodE.WaTaShiWaNo1.ui.pixiv

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.BlackBloodE.WaTaShiWaNo1.GlobalVariable
import com.BlackBloodE.WaTaShiWaNo1.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class PixivFragment : Fragment(), View.OnClickListener {

    private lateinit var pixivViewModel: PixivViewModel

    private lateinit var btnOK : Button
    private lateinit var btnOPEN : Button
    private lateinit var editText : TextView
    private lateinit var textView3 : TextView
    private lateinit var rBWork : RadioButton
    private lateinit var rBWorker : RadioButton
    private lateinit var RG : RadioGroup
    private lateinit var adView : AdView
    private var link : String = ""
    private var choose : String = ""

    //建立共用變數類別
    val gv = GlobalVariable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pixivViewModel =
            ViewModelProviders.of(this).get(PixivViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_pixiv, container, false)
        btnOK = root.findViewById(R.id.btnOK)
        btnOK.setOnClickListener(this)
        btnOPEN = root.findViewById(R.id.btnOPEN)
        btnOPEN.setOnClickListener(this)
        editText = root.findViewById(R.id.editText)
        textView3 = root.findViewById(R.id.textView3)
        textView3.setOnClickListener(this)
        rBWork = root.findViewById(R.id.rBWork)
        rBWorker = root.findViewById(R.id.rBWorker)
        RG = root.findViewById(R.id.RG)
        RG.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radio: RadioButton = root.findViewById(checkedId)
                if (radio.text.equals("作品")){
                    choose = "work"
                }else if (radio.text.equals("用戶")){
                    choose = "worker"
                }
                Toast.makeText(context," 搜尋切換 : ${radio.text}",
                    Toast.LENGTH_SHORT).show()
            })
        // AdMob 初始化
        MobileAds.initialize(context, getString(R.string.admob_app_id))
        // 橫幅廣告
        adView = root.findViewById(R.id.adView)
        adView.loadAd(AdRequest.Builder().build())
        adView.isVisible = gv.getAdSwitch()

        return root
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnOK -> {
                if (choose.equals("work")){
                    if (editText.text.toString().equals("")){
                        textView3.setText("")
                        Toast.makeText(context,"請輸入ID",
                            Toast.LENGTH_SHORT).show()
                    }else{
                        link = "https://www.pixiv.net/artworks/"+editText.text
                        textView3.setText(link)
                        btnOPEN.isVisible = true
                    }
                }else if (choose.equals("worker")){
                    if (editText.text.toString().equals("")){
                        textView3.setText("")
                        Toast.makeText(context,"請輸入ID",
                            Toast.LENGTH_SHORT).show()
                    }else{
                        link = "https://www.pixiv.net/member.php?id="+editText.text
                        textView3.setText(link)
                        btnOPEN.isVisible = true
                    }
                }else{
                    Toast.makeText(context,"請選擇搜尋目標",
                        Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btnOPEN -> {
                if (!link.equals("")){
                    val fileName = Uri.parse(link)
                    val intent = Intent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.action = android.content.Intent.ACTION_VIEW
                    intent.setData(fileName)
                    startActivity(intent)
                }
            }
            R.id.textView3 -> {
                Toast.makeText(context,"已將連結複製到剪貼簿",
                    Toast.LENGTH_SHORT).show()
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label",textView3.text)
                clipboard.setPrimaryClip(clip)
            }
        }
    }
}
