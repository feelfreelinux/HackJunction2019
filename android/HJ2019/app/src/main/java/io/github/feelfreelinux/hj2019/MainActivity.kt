package io.github.feelfreelinux.hj2019

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, FloatingBalloon::class.java)
        intent.putExtra("HINT", "Doopa")
        ContextCompat.startForegroundService(this, intent)
    }
}
