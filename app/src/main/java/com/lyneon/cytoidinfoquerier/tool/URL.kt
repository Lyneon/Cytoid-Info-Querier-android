package com.lyneon.cytoidinfoquerier.tool

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL

fun URL.toBitmap():Bitmap = BitmapFactory.decodeStream(this.openStream())