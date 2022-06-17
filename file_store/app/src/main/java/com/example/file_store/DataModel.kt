package com.example.file_store

class DataModel {
    lateinit var name:String

    constructor(name: String) {
        this.name = name
    }

    fun set(nameOfFile:String)
       {
           name=nameOfFile
       }
        fun get():String{
            return name
        }



}