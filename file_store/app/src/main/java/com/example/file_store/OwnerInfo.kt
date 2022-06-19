package com.example.file_store

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.file_store.databinding.ActivityOwnerInfoBinding

class OwnerInfo : AppCompatActivity() {
    private lateinit var binding:ActivityOwnerInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOwnerInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent=getIntent()
        val extra=intent.extras
        binding.fileName.text=extra!!.getString("FileName")
        binding.ownerName.text=extra.getString("OwnerName")
        binding.ownerEmail.text=extra.getString("OwnerEmail")

        binding.backButton.setOnClickListener{
            val ActiviyName=extra.getString("BackActivity")
            if(ActiviyName=="ShowFiles") {
                val intent = Intent(this,ShowFiles::class.java)
                startActivity(intent)
            }
            else
            {
                val intent = Intent(this,UserFiles::class.java)
                startActivity(intent)
            }
        }
    }
}