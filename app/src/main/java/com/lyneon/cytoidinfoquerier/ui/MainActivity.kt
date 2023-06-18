package com.lyneon.cytoidinfoquerier.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.databinding.ActivityMainBinding
import com.lyneon.cytoidinfoquerier.logic.DataParser
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.tool.showToast
import com.lyneon.cytoidinfoquerier.tool.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonQueryB30.setOnClickListener {
            val playerName = binding.editTextTextPlayerName.text.toString()
            "开始查询${playerName}\n请等待当前查询结束".showToast()
            binding.progressBarQuery.progress = 0
            binding.textViewProgress.text = "0/30"
            binding.buttonQueryB30.isClickable = false
            thread {
                val records = NetRequest.getB30Records(playerName, 30)
                val recordList = ArrayList<Record>().toMutableList()
                var progress = 0
                for (record in records.data.profile.bestRecords) {
                   recordList.add(
                        Record(
                            URL(record.chart.level.bundle.backgroundImage.thumbnail).toBitmap(),
                            DataParser.parseB30RecordToText(record)
                        )
                    )
                    progress++
                    runOnUiThread {
                        binding.textViewProgress.text = "${progress}/30"
                        binding.progressBarQuery.progress = progress
                    }
                }
                runOnUiThread {
                    binding.recyclerViewResult.adapter = B30RecordsAdapter(recordList)
                    binding.recyclerViewResult.layoutManager = LinearLayoutManager(this)
                    binding.buttonQueryB30.isClickable = true
                }
            }
        }
    }
}

class Record(val bgImage: Bitmap, val detail: String)

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
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = b30Records[position]
        holder.imageViewBackgroundImage.setImageBitmap(record.bgImage)
        holder.textViewRecordDetail.text = record.detail
    }

    override fun getItemCount(): Int = b30Records.size
}