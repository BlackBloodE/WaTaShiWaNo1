package com.BlackBloodE.WaTaShiWaNo1

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.BlackBloodE.WaTaShiWaNo1.R.id.nav_view
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    val manager = supportFragmentManager
    val transaction = manager.beginTransaction()

    override fun onCreate(savedInstanceState: Bundle?) {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) // 確認取消半透明設置。
        window.decorView.systemUiVisibility =
            (SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                    or SYSTEM_UI_FLAG_LAYOUT_STABLE) // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) // 跟系統表示要渲染 system bar 背景。
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setToolbar()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setFitsSystemWindows(true)
        drawerLayout.setClipToPadding(false)
        val navView: NavigationView = findViewById(nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_pixiv, R.id.nav_nhentai, R.id.nav_niconico,
                R.id.nav_settings, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //設置string.xml中的文字(獲取版本號)
        val res = resources
        val text = String.format(res.getString(R.string.nav_header_subtitle),getLocalVersion(this))
        //開啟app後自動開啟側邊抽屜
        Handler().postDelayed({
            drawerLayout.openDrawer(GravityCompat.START)
            tvVer.text = text
        },1500)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings){
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.nav_settings)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setToolbar() {
        // Set the padding to match the Status Bar height
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0)
    }
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources
            .getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
    //防止旋轉後Activity重啟用的((然而並沒有用
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 什麼都不用寫
        }
        else {
            // 什麼都不用寫
        }
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
