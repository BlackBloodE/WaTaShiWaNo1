package com.BlackBloodE.WaTaShiWaNo1.ui.niconico

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.BlackBloodE.WaTaShiWaNo1.R

class NiconicoFragment : Fragment(), View.OnClickListener {

    private lateinit var niconicoViewModel: NiconicoViewModel
    private lateinit var editText2 : EditText
    private lateinit var btnOk : Button
    private lateinit var btnOpen : Button
    private lateinit var textView4 : TextView
    private var link : String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        niconicoViewModel =
            ViewModelProviders.of(this).get(NiconicoViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_niconico, container, false)
        editText2 = root.findViewById(R.id.editText2)
        btnOk = root.findViewById(R.id.btnOk)
        btnOk.setOnClickListener(this)
        btnOpen = root.findViewById(R.id.btnOpen)
        btnOpen.setOnClickListener(this)
        textView4 = root.findViewById(R.id.textView4)
        textView4.setOnClickListener(this)
        return root
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnOk -> {
                if (editText2.text.toString().equals("")){
                    textView4.setText("")
                    Toast.makeText(context,"請輸入ID",
                        Toast.LENGTH_SHORT).show()
                }else{
                    link = "https://www.nicovideo.jp/watch/sm"+editText2.text
                    textView4.setText(link)
                    btnOpen.isVisible = true
                }
            }
            R.id.btnOpen -> {
                if (!link.equals("")){
                    val fileName = Uri.parse(link)
                    val intent = Intent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.action = android.content.Intent.ACTION_VIEW
                    intent.setData(fileName)
                    startActivity(intent)
                }
            }
            R.id.textView4 -> {
                Toast.makeText(context,"已將連結複製到剪貼簿",
                    Toast.LENGTH_SHORT).show()
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label",textView4.text)
                clipboard.setPrimaryClip(clip)
            }
        }
    }
}