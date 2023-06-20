package com.lyneon.cytoidinfoquerier.tool

import android.app.Activity
import android.content.Intent

inline fun <reified T> Activity.startActivity(block:Intent.() -> Unit){
    val intent = Intent()
    intent.setClass(this,T::class.java)
    intent.block()
    this.startActivity(intent)
}