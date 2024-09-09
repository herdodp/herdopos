package com.example.toolbar

import DatabaseHelper
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class view_struk : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_struk)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val getviewstruk = intent.getStringExtra("viewstruk")

        val databasehelper = DatabaseHelper(this@view_struk)
        val stringstruk = databasehelper.gettransaksiBykodestruk(getviewstruk.toString())

        val viewstruk1 = findViewById<TextView>(R.id.viewstruk)
        viewstruk1.text = stringstruk.toString()






    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        //super.onBackPressed()
        finish()
    }

}