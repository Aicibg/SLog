package com.hao.loglib

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider

class SLogDialog {

    companion object {
        val Instance: SLogDialog by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SLogDialog()
        }
    }

    private var dialog: AlertDialog? = null
    private var buttonShare: Button? = null
    private var buttonOpen: Button? = null
    private var buttonClear: Button? = null

    fun show(context: Context) {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = 24
        layoutParams.bottomMargin = 24
        layoutParams.leftMargin = 32
        layoutParams.rightMargin = 32
        buttonOpen = Button(context)
        buttonOpen?.let {
            it.id = R.id.buttonOpen
            it.setText(R.string.text_open)
            it.textSize = 16f
            it.setTextColor(Color.WHITE)
            it.setBackgroundColor(Color.parseColor("#00cc66"))
            it.setOnClickListener(onClickListener)
        }

        buttonShare = Button(context)
        buttonShare?.let {
            it.id = R.id.buttonShare
            it.setText(R.string.text_share)
            it.textSize = 16f
            it.setTextColor(Color.WHITE)
            it.setBackgroundColor(Color.parseColor("#00cc66"))
            it.setOnClickListener(onClickListener)
        }

        buttonClear = Button(context)
        buttonClear?.let {
            it.id = R.id.buttonClear
            it.setText(R.string.text_clear)
            it.textSize = 16f
            it.setTextColor(Color.WHITE)
            it.setBackgroundColor(Color.parseColor("#00cc66"))
            it.setOnClickListener(onClickListener)
        }
        linearLayout.addView(buttonOpen, layoutParams)
        linearLayout.addView(buttonShare, layoutParams)
        linearLayout.addView(buttonClear, layoutParams)

        dialog = AlertDialog.Builder(context)
            .setView(linearLayout)
            .show()
    }

    fun hide() {
        dialog?.dismiss()
    }

    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }

    private val onClickListener = View.OnClickListener {
        when (it.id) {
            R.id.buttonOpen -> {
                LogDetailsDialog.Instance.show(it.context)
            }
            R.id.buttonShare -> {
                SLog.slogD(" SLog.getLogFile()=${SLog.getLogFile()}")
                val share = Intent(Intent.ACTION_SEND)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val contentUri = FileProvider.getUriForFile(
                        it.context, it.context.applicationInfo.packageName+
                                ".loglib.fileprovider", SLog.getLogFile()
                    )
                    share.putExtra(Intent.EXTRA_STREAM, contentUri)
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(SLog.getLogFile()))
                }
                share.type = "text/plain" //此处可发送多种文件
                share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                it.context.startActivity(Intent.createChooser(share, "分享文件"))

            }
            R.id.buttonClear -> {
                SLog.clearLog()
            }
        }
        hide()
    }

}

