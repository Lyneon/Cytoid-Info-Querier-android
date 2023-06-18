package com.lyneon.cytoidinfoquerier

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseApplication.ActivityCollector.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.ActivityCollector.removeActivity(this)
    }
}