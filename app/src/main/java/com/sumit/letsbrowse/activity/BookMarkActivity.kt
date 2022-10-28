package com.sumit.letsbrowse.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sumit.letsbrowse.adapter.BookMarkAdapter
import com.sumit.letsbrowse.databinding.ActivityBookMarkBinding

class BookMarkActivity : AppCompatActivity() {
    lateinit var binding: ActivityBookMarkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookMarkBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.rvBookmarks.setHasFixedSize(true)
        binding.rvBookmarks.setItemViewCacheSize(5)
        binding.rvBookmarks.layoutManager  = LinearLayoutManager(this)
        binding.rvBookmarks.adapter = BookMarkAdapter(this,true)

    }
}
