package com.lyneon.cytoidinfoquerier.tool

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.LruCache
import android.view.View
import androidx.recyclerview.widget.RecyclerView


fun RecyclerView.toBitmap(): Bitmap {
    val adapter = this.adapter
    lateinit var bigBitmap: Bitmap
    if (adapter != null) {
        val size = adapter.itemCount
        var height = 0
        val paint = Paint()
        var iHeight = 0
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8
        val bitmaCache: LruCache<String, Bitmap> = LruCache(cacheSize)
        for (i in 0 until size) {
            val holder = adapter.createViewHolder(this, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(this.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            holder.itemView.layout(
                0, 0, holder.itemView.measuredWidth,
                holder.itemView.measuredHeight
            )
            holder.itemView.isDrawingCacheEnabled = true
            holder.itemView.buildDrawingCache()
            val drawingCache = holder.itemView.drawingCache
            if (drawingCache != null) {
                bitmaCache.put(i.toString(), drawingCache)
            }
            height += holder.itemView.measuredHeight
        }
        bigBitmap = Bitmap.createBitmap(this.measuredWidth, height, Bitmap.Config.ARGB_8888)
        val bigCanvas = Canvas(bigBitmap)
        val lBackground = this.background
        if (lBackground is ColorDrawable) {
            val lColor = lBackground.color
            bigCanvas.drawColor(lColor)
        }
        for (i in 0 until size) {
            val bitmap: Bitmap = bitmaCache.get(i.toString())
            bigCanvas.drawBitmap(bitmap, 0f, iHeight.toFloat(), paint)
            iHeight += bitmap.height
            bitmap.recycle()
        }
    }
    return bigBitmap
}