package com.hao.loglib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import java.lang.Exception

class LogDetailsDialog {

    companion object {
        val Instance: LogDetailsDialog by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            LogDetailsDialog()
        }
    }

    private var dialog: AlertDialog? = null
    private var logTextView: TextView? = null

    fun show(context: Context) {
        val scrollView = NestedScrollView(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = 24
        layoutParams.bottomMargin = 24
        layoutParams.leftMargin = 32
        layoutParams.rightMargin = 32
        logTextView = TextView(context).apply {
            textSize = 14f
        }
        scrollView.addView(logTextView, layoutParams)
        dialog = AlertDialog.Builder(context)
            .setView(scrollView)
            .setPositiveButton(R.string.text_confirm) { _, _ ->

            }
            .setNegativeButton(R.string.text_copy_text) { _, _ ->
                if (logTextView?.text.toString().isNotEmpty()) {
                    // 获取系统剪贴板
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                            as ClipboardManager
                    // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）,其他的还有
                    val clipData = ClipData.newPlainText(null, logTextView?.text.toString())
                    // 把数据集设置（复制）到剪贴板
                    clipboard.setPrimaryClip(clipData)
                }
            }
            .show()

        val logFile = SLog.getLogFile()
        val result = StringBuilder()
        try {
            val br = BufferedReader(
                InputStreamReader(
                    FileInputStream(logFile),
                    "UTF-8"
                )
            ) //构造一个BufferedReader类来读取文件
            var s: String? = null
            while (br.readLine().also { s = it } != null) { //使用readLine方法，一次读一行
                result.append(System.lineSeparator() + s)
            }
            br.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        logTextView?.let {
            it.text = result.toString()
        }
    }

    fun hide() {
        dialog?.hide()
    }

    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }

}

