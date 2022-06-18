package com.example.file_store

import android.net.Uri

class DataModel {
    lateinit var name:String
    lateinit var downloadurl: String

    constructor(name: String,downloadurl:String) {
        this.name = name
        this.downloadurl=downloadurl
    }

    @JvmName("setName1")
    fun setName(nameOfFile:String)
       {
           name=nameOfFile
       }

        @JvmName("getName1")
        fun getName():String{
            return name
        }

    fun setUrl(url:String)
    {
        downloadurl=url
    }
    fun getUrl():String{
        return downloadurl
    }





}