package com.hao.loglib

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import kotlin.math.abs
import android.util.DisplayMetrics


object SLog {
    var LOG_TAG = "SLog"

    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var logSaveLocal = false

//        val Instance: SLog by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
//            SLog()
//        }

    private lateinit var mContext: Application
    private var mWindowManager: WindowManager? = null
    private val logViewArray = SparseArray<TextView>()
    private val activityList = mutableListOf<Activity>()

    fun initLog(context: Application): SLog {
        mContext = context
        mContext.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activityList.contains(activity)) {
                    activityList.remove(activity)
                }
                activityList.add(activity)
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
                if (activityList.contains(activity)) {
                    activityList.remove(activity)
                }
                activityList.add(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                if (activityList.contains(activity)) {
                    activityList.remove(activity)
                }
            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
        val packageManager = context.packageManager as PackageManager
        val packageInfo = packageManager.getPackageInfo(
            context.packageName, 0
        )
        val labelRes = packageInfo.applicationInfo.labelRes
        val appName = context.resources.getString(labelRes)
        LOG_TAG = "$appName--SLog--"
        CatchException().attach(mContext)
        val file = File(mContext.externalCacheDir?.absolutePath, "${LOG_TAG}_log.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        return this
    }

    fun attachActivity(activity: AppCompatActivity) {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mWindowManager?.removeView(logViewArray[0])
                logViewArray.remove(0)
                super.onDestroy(owner)
            }
        })

        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        requestWindowPermission(activity)
    }

    fun saveLogFile(isSave: Boolean) {
        logSaveLocal = isSave
    }

    fun getApplicationContext(): Application {
        return mContext
    }

    fun getLogFile(): File {
        return File(mContext.externalCacheDir?.absolutePath, "${LOG_TAG}_log.txt")
    }

    fun clearLog() {
        File(mContext.externalCacheDir?.absolutePath, "${LOG_TAG}_log.txt").delete()
        File(mContext.externalCacheDir?.absolutePath, "${LOG_TAG}_log.txt").createNewFile()
    }

    fun getRunningActivity(): Activity {
        return activityList.last()
    }

    fun logD(message: String) {
        Log.d("$LOG_TAG->", message)
        if (!(this::mContext.isLateinit)) {
            throw Exception("please initLog first")
        }
        if (logSaveLocal) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val file =
                            File(mContext.externalCacheDir?.absolutePath, "${LOG_TAG}_log.txt")
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        file.appendText(
                            dateFormat.format(System.currentTimeMillis()) + " " + message + "\r\n",
                            Charset.defaultCharset()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun logD(exception: Exception) {
        if (!(this::mContext.isLateinit)) {
            throw Exception("please initLog first")
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val message = sw.toString()
        Log.d("$LOG_TAG->", message)
        if (logSaveLocal) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val file =
                            File(mContext.externalCacheDir?.absolutePath, "${LOG_TAG}_log.txt")
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        file.appendText(
                            dateFormat.format(System.currentTimeMillis()) + " " + message + "\r\n",
                            Charset.defaultCharset()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun logE(message: String) {
        Log.e("$LOG_TAG->", message)
        if (!(this::mContext.isLateinit)) {
            throw Exception("please initLog first")
        }
        if (logSaveLocal) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val file =
                        File(mContext.externalCacheDir?.absolutePath, "${LOG_TAG}_log.txt")
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    file.appendText(
                        dateFormat.format(System.currentTimeMillis()) + " " + message + "\r\n",
                        Charset.defaultCharset()
                    )
                }
            }
        }
    }

    fun logE(throwable: Throwable) {
        if (!(this::mContext.isLateinit)) {
            throw Exception("please initLog first")
        }
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        throwable.printStackTrace(writer)
        val buffer = stringWriter.buffer
        val message = buffer.toString()
        Log.e("$LOG_TAG->", message)
        if (logSaveLocal) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val file =
                        File(mContext.externalCacheDir?.absolutePath, "${LOG_TAG}_log.txt")
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    file.appendText(
                        dateFormat.format(System.currentTimeMillis()) + " " + message + "\r\n",
                        Charset.defaultCharset()
                    )
                }
            }
        }
    }

    fun logI(message: String) {
        Log.i("$LOG_TAG->", message)
        if (!(this::mContext.isLateinit)) {
            throw Exception("please initLog first")
        }
        if (logSaveLocal) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val file =
                        File(mContext.externalCacheDir?.absolutePath, "$LOG_TAG.txt")
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    file.appendText(
                        dateFormat.format(System.currentTimeMillis()) + " " + message + "\r\n",
                        Charset.defaultCharset()
                    )
                }
            }
        }
    }


    private fun requestWindowPermission(activity: AppCompatActivity) {
        //android 6.0或者之后的版本需要发一个intent让用户授权
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(mContext)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
//                startActivityForResult(intent,100)
                activity.registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) {
                    val data = it.data
                    val resultCode = it.resultCode
                    println("resultCode===$resultCode")
                    if (resultCode == 0) {
                        addLogView(activity)
                    }
                }.launch(intent)
            } else {
                addLogView(activity)
            }
        } else {
            addLogView(activity)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addLogView(activity: AppCompatActivity) {
        //创建窗口布局参数
        val mParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT
        )
        val outMetrics = DisplayMetrics()
        mWindowManager?.defaultDisplay?.getRealMetrics(outMetrics)
        val widthPixel = outMetrics.widthPixels
        val heightPixel = outMetrics.heightPixels
        //设置悬浮窗坐标
        mParams.x = widthPixel - 30
        mParams.y = heightPixel / 4
        //表示该Window无需获取焦点，也不需要接收输入事件
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mParams.gravity = Gravity.LEFT or Gravity.TOP
        Log.d("MainActivity", "sdk:" + Build.VERSION.SDK_INT)
        //设置window 类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //API Level 26
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        //创建悬浮窗(其实就创建了一个Button,这里也可以创建其他类型的控件)
        val logView = TextView(activity)
        logView.text = mContext.getString(R.string.text_log)
        logView.setTextColor(Color.WHITE)
        logView.textSize = 16f
        logView.gravity = Gravity.CENTER
        logView.setBackgroundResource(R.drawable.button_circle_red)
        var downX = 0
        var downY = 0
        logView.setOnTouchListener { v, event ->
            val rawX = event.rawX.toInt()
            val rawY = event.rawY.toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX.toInt()
                    downY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    val viewH = v.measuredHeight
                    val viewW = v.measuredWidth
                    mParams.x = rawX - viewW / 2
                    mParams.y = rawY - viewH / 2
                    if (abs(rawX - downX) > 5 || abs(rawY - downY) > 5) {
                        mWindowManager?.updateViewLayout(v, mParams)
                    }
                }
                MotionEvent.ACTION_UP -> {

                }
                else -> {
                }
            }
            false
        }
        logView.setOnClickListener {
            if (activityList.isEmpty()){
                return@setOnClickListener
            }
            if (SLogDialog.Instance.isShowing()) {
                SLogDialog.Instance.hide()
            } else {
                SLogDialog.Instance.show(activityList.last())
            }
        }
        mWindowManager?.addView(logView, mParams)
        val index = logViewArray.size()
        logViewArray.put(index, logView)
    }
}