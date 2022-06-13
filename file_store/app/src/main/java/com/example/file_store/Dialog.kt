package com.example.file_store

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater

class Dialog(val activity: Activity)
{
    private lateinit var Simpledialog:AlertDialog

    fun simpleloading(){
        val builder=AlertDialog.Builder(activity)
        val inflater=activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.simple_loading_spinner,null))
        builder.setCancelable(false)
        Simpledialog=builder.create()
        Simpledialog.show()
    }

    fun dismissSimpleDialog(){
        Simpledialog.dismiss()
    }

}