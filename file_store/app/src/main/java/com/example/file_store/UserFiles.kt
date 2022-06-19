package com.example.file_store

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserFiles : AppCompatActivity() {
    private lateinit var storage: StorageReference
    private lateinit var recyclerView: RecyclerView
    private var dataModel=ArrayList<DataModel>()
    private lateinit var adapter: UserFilesAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_files)

        firebaseAuth = FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance().getReference(firebaseAuth.currentUser!!.email.toString())
        recyclerView=findViewById(R.id.user_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager= LinearLayoutManager(this)

        if(dataModel.size>0)
            dataModel.clear()
        storage.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach{ item ->
                val itemName=item.name
                var itemOwnerName:String?=null
                var itemOwnerEmail:String?=null
                item.metadata.addOnSuccessListener {
                    itemOwnerName=it.getCustomMetadata("Name").toString()
                    itemOwnerEmail=it.getCustomMetadata("Email").toString()
                }
                var itemUrl:String
                Log.d("NAME_OF_FILE","Name IS $itemName")
                item.downloadUrl.addOnSuccessListener{
                    itemUrl=it.toString()
                    val dataitem=DataModel(itemName, itemUrl,itemOwnerName!!,itemOwnerEmail!!)
                    dataModel.add(dataitem)
                    adapter.notifyDataSetChanged()
                    Log.d("URL_OF_FILE","URL IS $itemUrl")
                }
                    .addOnFailureListener{
                        Toast.makeText(this,"Failed to fetch url", Toast.LENGTH_SHORT).show()
                    }

            }

            adapter=UserFilesAdapter(this,dataModel)
            recyclerView.adapter=adapter

        }
            .addOnFailureListener{
                Toast.makeText(this,"Failed while fetching data", Toast.LENGTH_SHORT).show()
            }

    }
    }