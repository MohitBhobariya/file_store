package com.example.file_store

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.w3c.dom.Text
import java.io.ByteArrayInputStream
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class DecryptFiles : AppCompatActivity() {

    val PDF: Int = 0
    val DOCX: Int = 1
    val AUDIO: Int = 2
    val VIDEO: Int = 3
    var sk: SecretKey? = null
    var uri: Uri? =null
    private lateinit var dialog: Dialog
    private lateinit var decryptobj: Decryption
    private lateinit var fileToBeDecrypted:TextView
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt_files)

        val pdfBtn = findViewById<View>(R.id.pdfBt) as Button
        val docxBtn = findViewById<View>(R.id.docxBt) as Button
        val musicBtn = findViewById<View>(R.id.audioBt) as Button
        val videoBtn = findViewById<View>(R.id.videoBt) as Button
        val startDecryption = findViewById<View>(R.id.start_decryption) as Button
        val done = findViewById<View>(R.id.done_button) as Button
        val decryption_key=findViewById<EditText>(R.id.decryption_key)
        fileToBeDecrypted=findViewById<TextView>(R.id.file_to_be_decrypted)
        dialog = Dialog(this)
        decryptobj = Decryption()

        done.setOnClickListener{
            val enteredString=decryption_key.text.toString()
            if(enteredString.length > 1) {
                sk = string_to_sk(enteredString)
                pdfBtn.isVisible=true
                docxBtn.isVisible=true
                musicBtn.isVisible=true
                videoBtn.isVisible=true
                startDecryption.isVisible=true

            }
            else
                Toast.makeText(this,"Enter Decryption Key",Toast.LENGTH_SHORT).show()
        }

        pdfBtn.setOnClickListener {
            if (checkingPermission()) {
                Toast.makeText(this, "Select PDF", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                }
                startActivityForResult(intent, PDF)
            } else {
                requestPermission()
            }
        }

        docxBtn.setOnClickListener {
            if (checkingPermission()) {
                Toast.makeText(this, "Select DOCX", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/docx/*"
                }
                startActivityForResult(intent, DOCX)
            } else {
                requestPermission()
            }
        }


        videoBtn.setOnClickListener {
            if (checkingPermission()) {
                Toast.makeText(this, "Select Video", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "video/*"
                }
                startActivityForResult(intent, VIDEO)
            } else {
                requestPermission()
            }
        }

        musicBtn.setOnClickListener {
            if (checkingPermission()) {
                Toast.makeText(this, "Select Audio", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                }
                startActivityForResult(intent, AUDIO)
            } else {
                requestPermission()
            }
        }

        startDecryption.setOnClickListener {
            if (uri != null) {
                try {
                    dialog.simpleloading()
                    val filedata = readFile(uri!!)
                    val decryptedata = decryptobj.decrypt(sk!!, filedata)
                    saveFile(decryptedata, uri!!)
                    dialog.dismissSimpleDialog()
                    Toast.makeText(this, "File Decrypted Successfully", Toast.LENGTH_SHORT).show()
                }
                catch (e: Exception) {
                    dialog.dismissSimpleDialog()
                    Toast.makeText(this, "Error while Decrypting", Toast.LENGTH_SHORT).show()
                    Log.d("Encrypt Error", "Error while Encrypting file")
                }
            } else {
                Toast.makeText(this, "Please Select File", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // dialog.simpleloading()

        if (resultCode == Activity.RESULT_OK) {
            uri = data!!.data!!

            var filename: String? = null
            data.data.let { returnUri ->
                contentResolver.query(returnUri!!, null, null, null, null)
            }?.use { cursor ->
                /*
                 * Get the column indexes of the data in the Cursor,
                 * move to the first row in the Cursor, get the data,
                 * and display it.
                 */
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                filename = cursor.getString(nameIndex)
            }
            fileToBeDecrypted.text=filename.toString()

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun checkingPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        } else {
            val readcheck =
                ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            val writecheck =
                ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            return readcheck == PackageManager.PERMISSION_GRANTED && writecheck == PackageManager.PERMISSION_GRANTED
        }
    }


    fun requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.setData(
                    Uri.parse(
                        String.format(
                            "package:%s",
                            applicationContext.packageName
                        )
                    )
                )
                activityResultLauncher.launch(intent)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activityResultLauncher.launch(intent)

            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, 30)
        }
    }


    val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            30 -> {
                if (grantResults.isNotEmpty()) {
                    val readper = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeper = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (readper && writeper) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "You Denied the Permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Throws(Exception::class)
    fun readFile(uri: Uri): ByteArray {
        val readBytes = contentResolver.openInputStream(uri)!!.readBytes()
        val inputStream = ByteArrayInputStream(readBytes).readBytes()
        return inputStream
    }

    @Throws(Exception::class)
    fun saveFile(fileData: ByteArray, uri: Uri) {
        val fos = contentResolver.openOutputStream(uri)
        if (fos != null) {
            fos.write(fileData)
        }
        fos!!.close()
    }

    fun string_to_sk(secretKey: String): SecretKey {
        val decodedKey = Base64.decode(secretKey, Base64.NO_WRAP)
        val originalKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")

        return originalKey
    }
}