package com.adwardstark.nsd_example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.adwardstark.nsd_example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinder: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinder = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinder.root)

        init()
    }

    private fun init() {
        viewBinder.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterServiceActivity::class.java))
        }

        viewBinder.discoverButton.setOnClickListener {
            startActivity(Intent(this, DiscoverServiceActivity::class.java))
        }
    }
}