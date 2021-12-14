package com.hao.loglib

import android.app.Application
import android.content.Context
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset

class CatchException : Thread.UncaughtExceptionHandler {
    private var context: Context? = null
    var exceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun attach(application: Application?) {
        context = application
        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            if (SLog.logSaveLocal) {
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        val file =
                            File(
                                SLog.getApplicationContext().cacheDir.absolutePath,
                                "${SLog.LOG_TAG}_log.txt"
                            )
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        val sw = StringWriter()
                        val pw = PrintWriter(sw)
                        e.printStackTrace(pw)
                        val message = sw.toString()
                        file.appendText(
                            SLog.dateFormat.format(System.currentTimeMillis()) + " " + message + "\r\n",
                            Charset.defaultCharset()
                        )
                    }
                }
            }
        } catch (e1: FileNotFoundException) {
            e1.printStackTrace()
        }
        if (exceptionHandler != null) {
            exceptionHandler!!.uncaughtException(t, e)
        }
        Process.killProcess(Process.myPid())
    }
}