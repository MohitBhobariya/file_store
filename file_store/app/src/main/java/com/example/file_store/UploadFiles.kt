package com.example.file_store

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.file_store.databinding.ActivityUserBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import java.io.ByteArrayInputStream
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class UploadFiles : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    val PDF: Int = 0
    val DOCX: Int = 1
    val AUDIO: Int = 2
    val VIDEO: Int = 3
    var sk: SecretKey? = null
    lateinit var uri: Uri
    lateinit var mStorage: StorageReference
    lateinit var user_folder: StorageReference
    private lateinit var dialog: Dialog
    private lateinit var encryptobj: Encryption
    private lateinit var decryptobj: Decryption
    private lateinit var user_email:String
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_files)

        val pdfBtn = findViewById<View>(R.id.pdfBtn) as Button
        val docxBtn = findViewById<View>(R.id.docxBtn) as Button
        val musicBtn = findViewById<View>(R.id.audioBtn) as Button
        val videoBtn = findViewById<View>(R.id.videoBtn) as Button

        firebaseAuth = FirebaseAuth.getInstance()
        mStorage = FirebaseStorage.getInstance().getReference("Uploads")
        user_email= firebaseAuth.currentUser!!.email.toString()
        user_folder=FirebaseStorage.getInstance().getReference(user_email)


        dialog = Dialog(this)
        encryptobj = Encryption()
        decryptobj = Decryption()


        key()


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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {

            dialog.simpleloading()

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

                upload_to_user_folder(filename)

                try {
                    val filedata = readFile(uri)
                    val encodedata = encryptobj.encrypt(sk!!, filedata)
                    saveFile(encodedata, uri)
                    upload(filename!!)
                } catch (e: Exception) {
                    dialog.dismissSimpleDialog()
                    Toast.makeText(this, "Error while Encrypting", Toast.LENGTH_SHORT).show()
                    Log.d("Encrypt Error", "Error while Encrypting file")
                }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun upload_to_user_folder(filename: String?) {
        val mReference = user_folder.child(filename!!)
        val metdata= storageMetadata {
            setCustomMetadata("Name",firebaseAuth.currentUser!!.displayName.toString())
            setCustomMetadata("Email",firebaseAuth.currentUser!!.email)
        }

        try {
            mReference.putFile(uri,metdata).addOnSuccessListener {
                Toast.makeText(this, "Successfully Uploaded to User Folder", Toast.LENGTH_LONG).show()
                // dialog.dismissSimpleDialog()
            }
        } catch (e: Exception) {
            dialog.dismissSimpleDialog()
            Toast.makeText(this,"Error while uploading to database", Toast.LENGTH_LONG).show()
        }
    }

    private fun upload(filename: String) {
        val mReference = mStorage.child(filename)
        val metdata= storageMetadata {
            setCustomMetadata("Name",firebaseAuth.currentUser!!.displayName.toString())
            setCustomMetadata("Email",firebaseAuth.currentUser!!.email)
        }

        try {
            mReference.putFile(uri,metdata).addOnSuccessListener {
                Toast.makeText(this, "Successfully Uploaded", Toast.LENGTH_LONG).show()
                dialog.dismissSimpleDialog()
            }
        } catch (e: Exception) {
            dialog.dismissSimpleDialog()
            Toast.makeText(this,"Error while uploading to database", Toast.LENGTH_LONG).show()
        }

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

    fun key(){
        dialog.simpleloading()


        db = FirebaseFirestore.getInstance()

        val users = db.collection("USERS")
        val firebaseuser = firebaseAuth.currentUser
        val email = firebaseuser!!.email
        val name = firebaseuser.displayName.toString()

        val docref = db.collection("USERS").document(email!!)
        docref.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    if (document.exists()) {
                        val sk_in_string = document["SecretKey"].toString()
                        sk=string_to_sk(sk_in_string)
                        Log.d("TAG", "Document already exists.")
                        dialog.dismissSimpleDialog()
                        Toast.makeText(this,"Key already exist",Toast.LENGTH_SHORT).show()
                    }

                    else {
                        sk = generateSecretKey()
                        val sktostring=sk_to_string(sk!!)
                        val user = hashMapOf(
                            "Name" to name,
                            "Email" to email,
                            "SecretKey" to sktostring
                        )
                        users.document(email).set(user)

                        Log.d("TAG", "Document inserted.")
                        dialog.dismissSimpleDialog()
                        Toast.makeText(this,"Key inserted",Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                dialog.dismissSimpleDialog()
                Log.d("TAG", "Error: ", task.exception)
                Toast.makeText(this,"Error while genrating key",Toast.LENGTH_SHORT).show()
            }
        }

    }


    @Throws(Exception::class)
    fun generateSecretKey(): SecretKey? {
        val secureRandom = SecureRandom()
        val keyGenerator = KeyGenerator.getInstance("AES")
        //generate a key with secure random
        keyGenerator?.init(128, secureRandom)
        return keyGenerator?.generateKey()
    }


    fun sk_to_string(secretKey: SecretKey): String {
        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
        return encodedKey
    }


    fun string_to_sk(secretKey: String): SecretKey {
        val decodedKey = Base64.decode(secretKey, Base64.NO_WRAP)
        val originalKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")

        return originalKey
    }
}