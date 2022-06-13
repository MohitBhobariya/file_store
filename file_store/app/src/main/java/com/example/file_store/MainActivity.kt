package com.example.file_store

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.file_store.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding:ActivityMainBinding
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding= ActivityMainBinding.inflate(layoutInflater)
        dialog=Dialog(this)
        setContentView(binding.root)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1088350226131-vjc1f4ves53hb2d24a993jv5kf6sbs5s.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth= FirebaseAuth.getInstance()



        binding.googleSignInButton.setOnClickListener {
            val intent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(intent)
        }

    }


    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(applicationContext,"Login Succesfull",Toast.LENGTH_SHORT).show()
            // There are no request codes
            val data: Intent? = result.data
            val accountTask=GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogleAccount(account)
            }
            catch (e: ApiException)
            {
                Toast.makeText(applicationContext,"Login Failed", Toast.LENGTH_SHORT).show()
            }


        }
    }

    fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->

                // Sign in success, update UI with the signed-in User's information
                val firebaseuser = firebaseAuth.currentUser
                //val uid = firebaseuser!!.uid
                val email = firebaseuser!!.email
                val name=firebaseuser.displayName.toString()

//                if (authResult.additionalUserInfo!!.isNewUser)
//                {
//                    val users=db.collection("USERS")
//                    val User= hashMapOf(
//                        "Name" to name,
//                        "Email" to email
//                    )
//                    users.document(email!!).set(User)
//                    Toast.makeText(
//                        requireActivity(),
//                        "New User logged in with $email",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT)
                    .show()

                val intent=Intent(this,User::class.java)
                //intent.putExtra("Email",email)
                startActivity(intent)
                // dialog.dismissSignInDialog()
                Log.d("Success","Signin Sucessfull")
                //this.finish()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                // dialog.dismissSignInDialog()
            }

    }

}