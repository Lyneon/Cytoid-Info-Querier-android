package com.lyneon.cytoidinfoquerier

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import com.lyneon.cytoidinfoquerier.tool.startActivity
import com.lyneon.cytoidinfoquerier.ui.CrashActivity

class BaseApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        Thread.setDefaultUncaughtExceptionHandler { _, p1 ->
            startActivity<CrashActivity> {
                putExtra("e", p1.stackTraceToString())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}