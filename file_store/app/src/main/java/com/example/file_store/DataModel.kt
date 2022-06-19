package com.example.file_store

import android.net.Uri

class DataModel {
    lateinit var name:String
    lateinit var downloadurl: String
    lateinit var ownername:String
    lateinit var owneremail:String

    constructor(name: String, downloadurl: String, ownername: String, owneremail: String) {
        this.name = name
        this.downloadurl = downloadurl
        this.ownername = ownername
        this.owneremail = owneremail
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

    fun setOwnerName(ownerName:String)
    {
        ownername=ownerName
    }
    fun getOwnerName():String{
        return ownername
    }

    fun setOwnerEmail(ownerEmail:String)
    {
        owneremail=ownerEmail
    }
    fun getOwnerEmail():String{
        return owneremail
    }





}