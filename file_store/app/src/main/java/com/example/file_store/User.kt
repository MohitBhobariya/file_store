package com.example.file_store

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.telephony.mbms.FileInfo
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.file_store.databinding.ActivityUserBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class User : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var toogle: ActionBarDrawerToggle
    private lateinit var sharedpref: SharedPreferences
    lateinit var mGoogleSignInClient: GoogleSignInClient

    lateinit var binding: ActivityUserBinding
    lateinit var mStorage: StorageReference
    private lateinit var dialog: Dialog
    private lateinit var encryptobj: Encryption
    private lateinit var decryptobj: Decryption


    val permissions = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(findViewById(R.id.action_bar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedpref=this?.getPreferences(Context.MODE_PRIVATE)?:return
        val isLogin=sharedpref.getString("Email","1")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1088350226131-vjc1f4ves53hb2d24a993jv5kf6sbs5s.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)



        if(isLogin=="1")
        {
            val sharedEmail=intent.getStringExtra("Email")

            if(sharedEmail!=null)
            {
                with(sharedpref.edit())
                {
                    putString("Email", sharedEmail)
                    apply()
                }
            }

            else
            {
                val intent=Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        val upload_files = findViewById<View>(R.id.upload_files) as Button
        val download_files=findViewById<View>(R.id.download_files) as Button
        val user_files=findViewById<View>(R.id.user_files)
        val decrytfiles=findViewById<View>(R.id.decryptionBtn) as Button
        val decryptownfiles=findViewById<View>(R.id.decrypt_your_files) as Button
        val see_secret_key=findViewById<View>(R.id.see_secret_key) as Button

        val navView=findViewById<NavigationView>(R.id.nav_view_id)
        val drawayerLayout=findViewById<DrawerLayout>(R.id.drawer_layout)
        binding= ActivityUserBinding.inflate(layoutInflater)

        mStorage = FirebaseStorage.getInstance().getReference("Uploads")
        dialog = Dialog(this)
        encryptobj = Encryption()
        decryptobj = Decryption()
        firebaseAuth = FirebaseAuth.getInstance()


        val user=firebaseAuth.currentUser
        //toogle= ActionBarDrawerToggle(this,drawayerLayout,R.string.open,R.string.close)
        toogle= ActionBarDrawerToggle(this,drawayerLayout,findViewById(R.id.action_bar),R.string.open,R.string.close)
        drawayerLayout.addDrawerListener(toogle)
        toogle.isDrawerIndicatorEnabled=true
        toogle.syncState()

        supportActionBar?.setHomeButtonEnabled(true)
        val headerView=navView.getHeaderView(0)
        val userNameTextView=headerView.findViewById<TextView>(R.id.username)
        val userEmailId=headerView.findViewById<TextView>(R.id.useremail)
        val userImage=headerView.findViewById<CircleImageView>(R.id.userimage)

        userNameTextView.text=user?.displayName.toString()
        userEmailId.text=user?.email.toString()
        Glide.with(this).load(user?.photoUrl).into(userImage)
        navView.setNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.logout ->
                {
                    sharedpref.edit().remove("Email").apply()
                    dialog.simpleloading()
                    val email = getIntent().extras?.getString("Email")
                    mGoogleSignInClient.signOut()
                    val intent=Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this,"LogOut",Toast.LENGTH_SHORT).show()
                    dialog.dismissSimpleDialog()
                    finish()
                }
            }
            true
        }


        upload_files.setOnClickListener{
            val intent=Intent(this,UploadFiles::class.java)
            startActivity(intent)
        }
        user_files.setOnClickListener{
            val intent=Intent(this,UserFiles::class.java)
            startActivity(intent)
        }
        download_files.setOnClickListener{
            val intent=Intent(this,ShowFiles::class.java)
            startActivity(intent)
        }

        decrytfiles.setOnClickListener{
            val intent=Intent(this,DecryptFiles::class.java)
            startActivity(intent)
        }

        see_secret_key.setOnClickListener {
            val intent=Intent(this,SeeSecretKey::class.java)
            startActivity(intent)
        }

       decryptownfiles.setOnClickListener{
           val intent=Intent(this,DecryptOwnFiles::class.java)
           startActivity(intent)
       }



    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toogle.onConfigurationChanged(newConfig)
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toogle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toogle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    }