package com.lyneon.cytoidinfoquerier.ui

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.databinding.ActivityMainBinding
import com.lyneon.cytoidinfoquerier.logic.dao.DataParser
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.tool.loadStringFile
import com.lyneon.cytoidinfoquerier.tool.save
import com.lyneon.cytoidinfoquerier.tool.saveImageFile
import com.lyneon.cytoidinfoquerier.tool.saveStringFile
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.tool.startActivity
import com.lyneon.cytoidinfoquerier.tool.toBitmap
import java.io.BufferedInputStream
import java.net.URL
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.mainToolbar)

        val bufferedPlayerSharedPreference = getSharedPreferences("bufferedPlayer", MODE_PRIVATE)

        binding.textInputEditTextPlayerName.addTextChangedListener {
            if (it.isNullOrEmpty()) binding.textInputEditTextPlayerName.error = "玩家名不能为空"
            else binding.textInputEditTextPlayerName.error = null
        }

        binding.buttonQueryB30.setOnClickListener {
            if (binding.textInputEditTextPlayerName.length() == 0) {
                binding.textInputEditTextPlayerName.error = "玩家名不能为空"
                binding.textInputEditTextPlayerName.requestFocus()
                return@setOnClickListener
            }
            val playerName = binding.textInputEditTextPlayerName.text.toString()
            val bufferedPlayerTime = bufferedPlayerSharedPreference.getLong(playerName, -1L)
            if (bufferedPlayerTime != -1L && (System.currentTimeMillis() - bufferedPlayerTime) <= 21600000L) {
                "6小时内有查询记录，使用缓存数据".showToast()
                binding.progressBarQuery.progress = 0
                binding.buttonQueryB30.isClickable = false
                thread {
                    try {
                        val recordsString = loadStringFile(playerName)
                        val records = NetRequest.getB30Records(recordsString)
                        val records_count = records.data.profile.bestRecords.size
                        runOnUiThread {
                            binding.progressBarQuery.max = records_count
                            binding.textViewProgress.text = "0/${records_count}"
                        }
                        val recordList = ArrayList<Record>().toMutableList()
                        var progress = 0
                        for (record in records.data.profile.bestRecords) {
                            val bgImage =
                                BitmapFactory.decodeStream(BufferedInputStream(openFileInput("${playerName}_${record.chart.level.uid}")))
                            recordList.add(
                                Record(
                                    bgImage,
                                    DataParser.parseB30RecordToText(record),
                                    record.chart.level.bundle.backgroundImage.original
                                )
                            )
                            progress++
                            runOnUiThread {
                                binding.textViewProgress.text = "${progress}/${records_count}"
                                binding.progressBarQuery.progress = progress
                            }
                        }
                        runOnUiThread {
                            binding.progressBarQuery.progress = records_count
                            binding.recyclerViewResult.adapter = B30RecordsAdapter(recordList)
                            binding.recyclerViewResult.layoutManager = LinearLayoutManager(this)
                        }
                    } catch (e: Exception) {
                        this.startActivity<CrashActivity> {
                            putExtra("e", e.stackTraceToString())
                        }
                    } finally {
                        runOnUiThread {
                            binding.buttonQueryB30.isClickable = true
                        }
                    }
                }

            } else {
                "开始查询${playerName}\n请等待当前查询结束".showToast()
                bufferedPlayerSharedPreference.edit {
                    putLong(playerName, System.currentTimeMillis())
                    apply()
                }
                binding.progressBarQuery.progress = 0
                binding.buttonQueryB30.isClickable = false
                try {
                    thread {
                        val recordsString = NetRequest.getB30RecordsString(playerName, 30)
                        if (recordsString == null) {
                            throw Exception()
                        } else {
                            val records = NetRequest.getB30Records(recordsString)
                            saveStringFile(playerName, recordsString)
                            val records_count = records.data.profile.bestRecords.size
                            runOnUiThread {
                                binding.progressBarQuery.max = records_count
                                binding.textViewProgress.text = "0/${records_count}"
                            }
                            val cdl = CountDownLatch(records_count)
                            var progress = 0
                            for (record in records.data.profile.bestRecords) {
                                thread {
                                    val bgImage = try {
                                        URL(record.chart.level.bundle.backgroundImage.thumbnail).toBitmap()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        BitmapFactory.decodeResource(
                                            resources,
                                            R.drawable.sayakacry
                                        )
                                    }
                                    saveImageFile(
                                        "${playerName}_${record.chart.level.uid}",
                                        bgImage
                                    )
                                    progress++
                                    runOnUiThread {
                                        binding.textViewProgress.text =
                                            "${progress}/${records_count}"
                                        binding.progressBarQuery.progress = progress
                                    }
                                    cdl.countDown()
                                }
                            }
                            cdl.await()
                            thread {
                                try {
                                    val recordList = ArrayList<Record>().toMutableList()
                                    for (record in records.data.profile.bestRecords) {
                                        val bgImage =
                                            BitmapFactory.decodeStream(
                                                BufferedInputStream(
                                                    openFileInput("${playerName}_${record.chart.level.uid}")
                                                )
                                            )
                                        recordList.add(
                                            Record(
                                                bgImage,
                                                DataParser.parseB30RecordToText(record),
                                                record.chart.level.bundle.backgroundImage.original
                                            )
                                        )
                                    }
                                    runOnUiThread {
                                        "完成".showToast()
                                        binding.recyclerViewResult.adapter =
                                            B30RecordsAdapter(recordList)
                                        binding.recyclerViewResult.layoutManager =
                                            LinearLayoutManager(this)
                                    }
                                } catch (e: Exception) {
                                    this.startActivity<CrashActivity> {
                                        putExtra("e", e.stackTraceToString())
                                    }
                                } finally {
                                    runOnUiThread {
                                        binding.buttonQueryB30.isClickable = true
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    this.startActivity<CrashActivity> {
                        putExtra("e", e.stackTraceToString())
                    }
                } finally {
                    binding.buttonQueryB30.isClickable = true
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_save -> {
                "正在保存图片".showToast()
                thread {
                    val bitmap = binding.recyclerViewResult.toBitmap()
                    bitmap.save(contentResolver, ContentValues())
                    runOnUiThread {
                        "保存完成".showToast()
                    }
                }
            }
        }
        return true
    }
}

class Record(val bgImage: Bitmap, val detail: String, val bgImageSource: String)

class B30RecordsAdapter(private val b30Records: List<Record>) :
    RecyclerView.Adapter<B30RecordsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewBackgroundImage: ImageView =
            view.findViewById<ImageView>(R.id.imageView_recordBackground)
        val textViewRecordDetail: TextView =
            view.findViewById<TextView>(R.id.textView_recordDetail)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.imageViewBackgroundImage.setOnLongClickListener {
            val position = viewHolder.adapterPosition
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(b30Records[position].bgImageSource)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            BaseApplication.context.startActivity(intent)
            true
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = b30Records[position]
        holder.imageViewBackgroundImage.setImageBitmap(record.bgImage)
        holder.textViewRecordDetail.text = record.detail
    }

    override fun getItemCount(): Int = b30Records.size
}
