package com.example.file_store

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class SeeSecretKey : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: Dialog
    private lateinit var sk:String
    private lateinit var secretkeybox:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_secret_key)
        secretkeybox=findViewById<TextView>(R.id.secret_key_box)
        dialog= Dialog(this)
        firebaseAuth = FirebaseAuth.getInstance()

            key()
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
                         sk = document["SecretKey"].toString()
                        secretkeybox.text=sk
                        Log.d("TAG", "Document already exists.")
                        dialog.dismissSimpleDialog()
                        //Toast.makeText(this,"Key already exist", Toast.LENGTH_SHORT).show()
                    }

                    else {
                       val skey = generateSecretKey()
                        sk=sk_to_string(skey!!)
                        secretkeybox.text=sk
                        val user = hashMapOf(
                            "Name" to name,
                            "Email" to email,
                            "SecretKey" to sk
                        )
                        users.document(email).set(user)

                        Log.d("TAG", "Document inserted.")
                        dialog.dismissSimpleDialog()
                       // Toast.makeText(this,"Key inserted", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                dialog.dismissSimpleDialog()
                Log.d("TAG", "Error: ", task.exception)
                Toast.makeText(this,"Error while genrating key", Toast.LENGTH_SHORT).show()
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
}