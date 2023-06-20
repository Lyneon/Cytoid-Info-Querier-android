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
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.databinding.ActivityMainBinding
import com.lyneon.cytoidinfoquerier.logic.DataParser
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.tool.save
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.tool.startActivity
import com.lyneon.cytoidinfoquerier.tool.toBitmap
import java.net.URL
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.editTextTextPlayerName.addTextChangedListener {
            if (it.isNullOrEmpty()) binding.editTextTextPlayerName.error = "玩家名不能为空"
            else binding.editTextTextPlayerName.error = null
        }

        binding.buttonQueryB30.setOnClickListener {
            if (binding.editTextTextPlayerName.length() == 0) {
                binding.editTextTextPlayerName.error = "玩家名不能为空"
                binding.editTextTextPlayerName.requestFocus()
                return@setOnClickListener
            }
            val playerName = binding.editTextTextPlayerName.text.toString()
            "开始查询${playerName}\n请等待当前查询结束".showToast()
            binding.progressBarQuery.progress = 0
            binding.textViewProgress.text = "0/30"
            binding.buttonQueryB30.isClickable = false
            try {
                thread {
                    val records = NetRequest.getB30Records(playerName, 30)
                    val recordList = ArrayList<Record>().toMutableList()
                    var progress = 0
                    for (record in records.data.profile.bestRecords) {
                        recordList.add(
                            Record(
                                try {
                                    URL(record.chart.level.bundle.backgroundImage.thumbnail).toBitmap()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    BitmapFactory.decodeResource(resources, R.drawable.sayakacry)
                                },
                                DataParser.parseB30RecordToText(record),
                                record.chart.level.bundle.backgroundImage.original
                            )
                        )
                        progress++
                        runOnUiThread {
                            binding.textViewProgress.text = "${progress}/30"
                            binding.progressBarQuery.progress = progress
                        }
                    }
                    runOnUiThread {
                        "完成".showToast()
                        binding.progressBarQuery.progress = 30
                        binding.recyclerViewResult.adapter = B30RecordsAdapter(recordList)
                        binding.recyclerViewResult.layoutManager = LinearLayoutManager(this)
                    }
                }
            }catch (e:Exception){
                this.startActivity<CrashActivity> {
                    putExtra("e",e.stackTraceToString())
                }
            }finally {
                binding.buttonQueryB30.isClickable = true
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

class B30RecordsAdapter(val b30Records: List<Record>) :
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