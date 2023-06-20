package com.lyneon.cytoidinfoquerier

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.lyneon.cytoidinfoquerier.ui.CrashActivity
import com.lyneon.cytoidinfoquerier.tool.startActivity

class BaseApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    object ActivityCollector {
        private val activities = ArrayList<Activity>()

        fun addActivity(activity: Activity) = activities.add(activity)

        fun removeActivity(activity: Activity) = activities.remove(activity)

        fun finishAll() {
            for (activity in activities) {
                if (!activity.isFinishing) activity.finish()
            }
            activities.clear()
        }
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