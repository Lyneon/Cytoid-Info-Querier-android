package com.lyneon.cytoidinfoquerier.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.lyneon.cytoidinfoquerier.BaseActivity
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.databinding.ActivityCrashBinding
import com.lyneon.cytoidinfoquerier.tool.showToast

class CrashActivity : BaseActivity() {

    private lateinit var binding: ActivityCrashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textViewErrorMessage.text = intent.getStringExtra("e")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_crash, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_crash_copy -> {
                val clipboardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText(
                        "errorMessage",
                        binding.textViewErrorMessage.text.toString()
                    )
                )
                "已复制到剪贴板".showToast()
            }

//            R.id.menu_crash_restart -> {
//                startActivity<MainActivity> { }
//                finish()
//            }
        }
        return true
    }

}