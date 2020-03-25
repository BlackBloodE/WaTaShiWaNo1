package com.BlackBloodE.WaTaShiWaNo1

import android.R
import android.app.Activity
import android.app.Dialog
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window

/**
 * simple and powerful Keyboard show/hidden listener,view {@android.R.id.content} and {@ViewTreeObserver.OnGlobalLayoutListener}
 * Created by yes.cpu@gmail.com 2016/7/13.
 */
class KeyboardChangeListener private constructor(contextObj: Any?) :
    OnGlobalLayoutListener {
    private var mKeyboardListener: KeyboardListener? =
        null
    private var mShowFlag = false
    private var mWindow: Window? = null
    private var mContentView: View? = null

    interface KeyboardListener {
        /**
         * call back
         * @param isShow         true is show else hidden
         * @param keyboardHeight keyboard height
         */
        fun onKeyboardChange(isShow: Boolean, keyboardHeight: Int)
    }

    fun setKeyboardListener(keyboardListener: KeyboardListener?) {
        mKeyboardListener = keyboardListener
    }

    private fun findContentView(contextObj: Activity): View {
        return contextObj.findViewById(R.id.content)
    }

    private fun findContentView(contextObj: Dialog): View {
        return contextObj.findViewById(R.id.content)
    }

    private fun addContentTreeObserver() {
        mContentView!!.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        if (mContentView == null || mWindow == null) {
            return
        }
        val currentViewHeight = mContentView!!.getHeight()
        if (currentViewHeight == 0) {
            Log.d(TAG, "currHeight is 0")
            return
        }
        val screenHeight = screenHeight
        val windowBottom: Int
        val keyboardHeight: Int
        val rect = Rect()
        mWindow!!.getDecorView().getWindowVisibleDisplayFrame(rect)
        windowBottom = rect.bottom
        keyboardHeight = screenHeight - windowBottom
        Log.d(
            TAG,
            "onGlobalLayout() called  screenHeight $screenHeight VisibleDisplayHeight $windowBottom"
        )
        if (mKeyboardListener != null) {
            val currentShow =
                keyboardHeight > MIN_KEYBOARD_HEIGHT
            if (mShowFlag != currentShow) {
                mShowFlag = currentShow
                mKeyboardListener!!.onKeyboardChange(currentShow, keyboardHeight)
            }
        }
    }

    private val screenHeight: Int
        private get() {
            val defaultDisplay = mWindow!!.windowManager.defaultDisplay
            var screenHeight = 0
            val point = Point()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                defaultDisplay.getRealSize(point)
            } else {
                defaultDisplay.getSize(point)
            }
            screenHeight = point.y
            return screenHeight
        }

    fun destroy() {
        if (mContentView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mContentView!!.getViewTreeObserver().removeOnGlobalLayoutListener(this)
            }
        }
    }

    companion object {
        private const val TAG = "KeyboardChangeListener"
        const val MIN_KEYBOARD_HEIGHT = 300
        fun create(activity: Activity?): KeyboardChangeListener {
            return KeyboardChangeListener(activity)
        }

        fun create(dialog: Dialog?): KeyboardChangeListener {
            return KeyboardChangeListener(dialog)
        }
    }

    init {
        if (contextObj == null) {
            Log.d(TAG, "contextObj is null")

        }
        if (contextObj is Activity) {
            mContentView = findContentView(contextObj)
            mWindow = contextObj.window
        } else if (contextObj is Dialog) {
            mContentView = findContentView(contextObj)
            mWindow = contextObj.window
        }
        if (mContentView != null && mWindow != null) {
            addContentTreeObserver()
        }
    }
}