package com.example.toolbar


import android.view.LayoutInflater
import android.os.Bundle
import android.view.ViewGroup
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment



class ProdukFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceSate: Bundle?
    ): View?{
        val view = inflater.inflate(R.layout.produk_fragment, container, false)







        return view
    }

}