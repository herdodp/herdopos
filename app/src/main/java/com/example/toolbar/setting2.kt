package com.example.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text

@Suppress("DEPRECATION")
class setting2 : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting2)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        //  button nama toko
        val buttontokoname = findViewById<Button>(R.id.namatoko)
        buttontokoname.setOnClickListener{

            showDialogNamaToko()
        }

        //button alamat toko
        val buttonalamat = findViewById<Button>(R.id.buttonalamat)
        buttonalamat.setOnClickListener{
            setAlamatToko()
        }



        //share preferences
        val getsharepref  = getSharedPreferences("My_Prefs", Context.MODE_PRIVATE)
        val getshareprefalamat  = getSharedPreferences("alamat", Context.MODE_PRIVATE)

        //nama toko
        val getnamatokoprefs = getsharepref.getString("keynamatoko", "default")

        //  call nama toko
        val teksnamatoko2  = findViewById<TextView>(R.id.textnamatoko2)
        teksnamatoko2.text = getnamatokoprefs


        //alamat toko
        val getalamattoko = getshareprefalamat.getString("keyalamat", "default")

        //call alamat toko
        val alamatget = findViewById<TextView>(R.id.teksalamat)
        alamatget.text = getalamattoko








    }






    private fun showDialogNamaToko(){

        //inflace  costum layout
        val inflater = LayoutInflater.from(this)
        val dialogNamaToko = inflater.inflate(R.layout.edittextnamatoko, null)


        AlertDialog.Builder(this)
            .setTitle("Nama Toko")
            .setView(dialogNamaToko)
            .setCancelable(false)
            .setPositiveButton("Simpan"){_,_->

                //take value from edit text
                val editnamatoko = dialogNamaToko.findViewById<EditText>(R.id.inputnamatoko)
                val inputtextnamatoko = editnamatoko.text.toString()

                //save to share preference
                val sharepref = getSharedPreferences("My_Prefs", Context.MODE_PRIVATE)
                val  editor = sharepref.edit()
                editor.putString("keynamatoko", "$inputtextnamatoko")

                editor.apply()

                //cek
                val cekpref1 = sharepref.getString("keynamatoko", "Default")
                if(inputtextnamatoko == cekpref1){
                    Toast.makeText(applicationContext, "value : ${cekpref1}", Toast.LENGTH_LONG).show()
                    val teksnamatoko = findViewById<TextView>(R.id.textnamatoko2)
                    teksnamatoko.text = cekpref1

                }else{
                    Toast.makeText(applicationContext, "gagal menyimpan", Toast.LENGTH_LONG).show()
                }


            }
            .setNegativeButton("Batal"){dialog, _ ->
                dialog.dismiss()

            }
            .create()
            .show()


    }




    @SuppressLint("InflateParams", "MissingInflatedId")
    private fun setAlamatToko(){
        val layoutinflate = LayoutInflater.from(this)
        val viewalamat = layoutinflate.inflate(R.layout.alamat_toko, null)


        AlertDialog.Builder(this)
            .setTitle("Alamat Toko")
            .setView(viewalamat)
            .setCancelable(false)
            .setPositiveButton("Simpan"){_,_ ->

                val teksalamat = viewalamat.findViewById<EditText>(R.id.alamatToko)
                val inputteksalamat = teksalamat.text.toString()

                val shareprefalamat = getSharedPreferences("alamat", Context.MODE_PRIVATE)
                val editor = shareprefalamat.edit()
                editor.putString("keyalamat", "$inputteksalamat")
                editor.apply()

                //cek alamat
                val cekalamat = shareprefalamat.getString("keyalamat", "default")
                if(cekalamat == inputteksalamat){
                    Toast.makeText(applicationContext, "alamat : ${cekalamat}", Toast.LENGTH_SHORT).show()
                    val insertalamat = findViewById<TextView>(R.id.teksalamat)
                    insertalamat.text = cekalamat
                }else{
                    Toast.makeText(applicationContext, "Gagal menyimpan alamat", Toast.LENGTH_SHORT).show()
                }

            }
            .setNegativeButton("Batal"){default,_ ->
                default.dismiss()
            }
            .create()
            .show()


    }










    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}