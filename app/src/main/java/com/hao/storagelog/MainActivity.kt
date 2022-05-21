package com.hao.storagelog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hao.loglib.SLog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        SLog.attachLifecycle(this)
        SLog.attachActivity(this)

        findViewById<Button>(R.id.buttonView).setOnClickListener {
            SLog.logD("startActivity  ScrollingActivity")
            startActivity(Intent(this, ScrollingActivity::class.java))
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val permissionArray = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val requestPermissions = permissionArray.filter {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) !== PackageManager.PERMISSION_GRANTED
        }
        if (requestPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                requestPermissions.toTypedArray(),
                6
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        SLog.removeLogView()
    }
}