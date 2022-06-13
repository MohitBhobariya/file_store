package com.example.file_store

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.file_store.databinding.ActivityUserBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class User : AppCompatActivity() {

    lateinit var binding:ActivityUserBinding
    val PDF : Int = 0
    val DOCX : Int = 1
    val AUDIO : Int = 2
    val VIDEO : Int = 3
    lateinit var uri : Uri
    lateinit var mStorage : StorageReference
    private lateinit var dialog: Dialog
    private lateinit var encrypt:Encryption

    override fun onCreate(savedInstanceState: Bundle?) {

       // Toast.makeText(this, "In User Activity :)", Toast.LENGTH_LONG).show()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        val pdfBtn = findViewById<View>(R.id.pdfBtn) as Button
        val docxBtn = findViewById<View>(R.id.docxBtn) as Button
        val musicBtn = findViewById<View>(R.id.musicBtn) as Button
        val videoBtn = findViewById<View>(R.id.videoBtn) as Button
        // binding= ActivityUserBinding.inflate(layoutInflater)

        mStorage = FirebaseStorage.getInstance().getReference("Uploads")
        dialog= Dialog(this)
        encrypt=Encryption()

        pdfBtn.setOnClickListener{
                val intent = Intent()
            intent.setType ("application/pdf")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF)
        }

        docxBtn.setOnClickListener{
            val intent = Intent()
            Toast.makeText(this,"Docx Button Pressed",Toast.LENGTH_LONG).show()
            intent.setType ("application/docx/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select DOCX"), DOCX)
        }

        musicBtn.setOnClickListener(View.OnClickListener {
                view: View? -> val intent = Intent()
            intent.setType ("audio/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Audio"), AUDIO)
        })

        videoBtn.setOnClickListener(View.OnClickListener {
                view: View? -> val intent = Intent()
            intent.setType ("video/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO)
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       // val uriTxt = findViewById<View>(R.id.uriTxt) as TextView
        dialog.simpleloading()
        if (resultCode == RESULT_OK) {
            if (requestCode == PDF) {
                uri = data!!.data!!
                try {
                    val filedata = encrypt.readFile(uri.path!!)
                    val secretkey = encrypt.generateSecretKey()
                    val encodedata = encrypt.encrypt(secretkey!!, filedata)
                    encrypt.saveFile(encodedata, uri.path!!)
                }
                catch (e:Exception)
                {
                    Toast.makeText(this,"Error while Encrypting",Toast.LENGTH_SHORT).show()
                    Log.d("Encrypt Error","Error while Encrypting file")
                }

           //     uriTxt.text = uri.toString()
                upload ()
            }
            else if (requestCode == DOCX) {
                uri = data!!.data!!
           //     uriTxt.text = uri.toString()
                upload ()
            }else if (requestCode == AUDIO) {
                uri = data!!.data!!
            //    uriTxt.text = uri.toString()
                upload ()
            }else if (requestCode == VIDEO) {
                uri = data!!.data!!
          //      uriTxt.text = uri.toString()
                upload ()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun upload() {
        var mReference = mStorage.child(uri.lastPathSegment!!)
        try {
            mReference.putFile(uri).addOnSuccessListener {
//                    taskSnapshot: UploadTask.TaskSnapshot? -> var url = taskSnapshot!!.downloadUrl
//                val dwnTxt = findViewById<View>(R.id.dwnTxt) as TextView
//                dwnTxt.text = url.toString()
                Toast.makeText(this, "Successfully Uploaded :)", Toast.LENGTH_LONG).show()
                 dialog.dismissSimpleDialog()

            }
        }catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }

    }

   }