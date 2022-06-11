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
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import kotlin.math.abs
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference
import java.util.concurrent.ScheduledThreadPoolExecutor


object SLog {
    var LOG_TAG = "SLog"

    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var logSaveLocal = false
    const val REQUEST_CODE = 10010

//        val Instance: SLog by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
//            SLog()
//        }

    private lateinit var mContext: Application
    private var mWindowManager: WindowManager? = null
    private val logViewArray = SparseArray<TextView>()
    private val activityList = mutableListOf<Activity>()
    private val threadPool = ScheduledThreadPoolExecutor(2)
    private var attachActivity: WeakReference<Activity>? = null

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
                if (activityList.size == 1 && logViewArray.size() == 1) {
                    logViewArray[0].visibility = View.VISIBLE
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (activityList.contains(activity)) {
                    activityList.remove(activity)
                }
                if (activityList.isEmpty()) {
                    logViewArray[0].visibility = View.GONE
                }
            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                if (activity == attachActivity?.get()) {
                    removeLogView()
                }
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


    fun attachLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mWindowManager?.removeView(logViewArray[0])
                logViewArray.remove(0)
                super.onDestroy(owner)
            }
        })

        if (lifecycleOwner is AppCompatActivity) {
            mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            requestWindowPermission(lifecycleOwner)
        }
    }

    fun addRunnable(runnable: Runnable) {
        threadPool.execute(runnable)
    }

    /**
     * attachActivity  addAttachLogView  removeLogView配合使用
     */
    fun attachActivity(activity: Activity) {
        attachActivity = WeakReference(activity)
        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        requestWindowPermission(activity)
    }

    fun addAttachLogView(activity: Activity) {
        addLogView(activity)
    }

    private fun removeLogView() {
        mWindowManager?.removeView(logViewArray[0])
        logViewArray.remove(0)
    }

    fun saveLogFile(isSave: Boolean): SLog {
        logSaveLocal = isSave
        return this
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

    fun slogD(message: String) {
        Log.d("$LOG_TAG->", message)
        if (logSaveLocal) {
            threadPool.execute {
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

    fun slogD(exception: Exception) {
        if (!(this::mContext.isLateinit)) {
            throw Exception("please initLog first")
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val message = sw.toString()
        Log.d("$LOG_TAG->", message)
        if (logSaveLocal) {
            threadPool.execute {
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

    fun slogE(message: String) {
        Log.e("$LOG_TAG->", message)
        if (!(this::mContext.isLateinit)) {
            throw Exception("please initLog first")
        }
        if (logSaveLocal) {
            threadPool.execute {
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

    fun slogE(throwable: Throwable) {
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
            threadPool.execute {
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

    fun slogI(message: String) {
        Log.i("$LOG_TAG->", message)
        if (!(this::mContext.isLateinit)) {
            throw Exception("please initLog first")
        }
        if (logSaveLocal) {
            threadPool.execute {
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


    private fun requestWindowPermission(activity: Activity) {
        //android 6.0或者之后的版本需要发一个intent让用户授权
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(mContext)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
                when (activity) {
                    is AppCompatActivity -> {
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
                    }
                    is FragmentActivity -> {
                        val manager = activity.supportFragmentManager
                        var fragment =
                            manager.findFragmentByTag(ActivityResultFragment.NAME) as? ActivityResultFragment
                        if (fragment == null) {
                            fragment = ActivityResultFragment()
                            manager.beginTransaction().add(fragment, ActivityResultFragment.NAME)
                                .commitNowAllowingStateLoss()
                        }
                        fragment.callback = { requestCode, _, _ ->
                            val success = requestCode == ActivityResultFragment.REQUEST_CODE
                            if (success) {
                                addLogView(activity)
                            }
                        }
                        try {
                            fragment.startActivityForResult(
                                intent,
                                ActivityResultFragment.REQUEST_CODE
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            fragment.callback = null
                        }
                    }
                    else -> {
                        activity.startActivityForResult(intent, REQUEST_CODE)
                    }
                }
            } else {
                addLogView(activity)
            }
        } else {
            addLogView(activity)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addLogView(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(mContext)
        ) {
            return
        }
        if (logViewArray.size() > 0) {
            return
        }
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
            if (activityList.isEmpty()) {
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

typealias ActivityResultCallback = (requestCode: Int, resultCode: Int, data: Intent?) -> Unit

class ActivityResultFragment : Fragment() {

    companion object {
        const val NAME = "ActivityResultFragment_op"
        const val REQUEST_CODE = 18745
    }

    var callback: ActivityResultCallback? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callback?.invoke(requestCode, resultCode, data)
        callback = null
    }
}