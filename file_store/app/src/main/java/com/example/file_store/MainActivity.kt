package com.example.file_store

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding= ActivityMainBinding.inflate(layoutInflater)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1083604959054-43th3co7arldu77mb1r6qb9jj57f6jc1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth= FirebaseAuth.getInstance()

        binding.googleSignInButton.setOnClickListener {
            val intent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(intent)
        }

    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            //Toast.makeText(requireContext(),"Login Succesfull",Toast.LENGTH_SHORT).show()
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

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->

                // Sign in success, update UI with the signed-in user's information
                val firebaseuser = firebaseAuth.currentUser
                //val uid = firebaseuser!!.uid
                val email = firebaseuser!!.email
                val name=firebaseuser.displayName.toString()

//                if (authResult.additionalUserInfo!!.isNewUser)
//                {
//                    val users=db.collection("USERS")
//                    val user= hashMapOf(
//                        "Name" to name,
//                        "Email" to email
//                    )
//                    users.document(email!!).set(user)
//                    Toast.makeText(
//                        requireActivity(),
//                        "New user logged in with $email",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT)
                    .show()

                val intent=Intent(this,user::class.java)
                intent.putExtra("Email",email)
                startActivity(intent)
               // dialog.dismissSignInDialog()
                this.finish()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
               // dialog.dismissSignInDialog()
            }

    }
}