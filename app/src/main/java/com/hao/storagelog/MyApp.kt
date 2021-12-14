package com.hao.storagelog

import android.app.Application
import com.hao.loglib.SLog

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SLog.initLog(this).saveLogFile(true)
    }
}