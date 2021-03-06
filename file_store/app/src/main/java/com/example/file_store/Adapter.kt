package com.example.file_store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.widget.Toast


class Adapter(val showFiles: ShowFiles, val dataModel: ArrayList<DataModel>) :
    RecyclerView.Adapter<MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater:LayoutInflater= LayoutInflater.from(showFiles.baseContext)
        val view:View = layoutInflater.inflate(R.layout.elements,null,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.filename.setText(dataModel.get(position).getName())
        holder.download.setOnClickListener{
            downloadFile(holder.filename.context,dataModel.get(position).getName(),".pdf",DIRECTORY_DOWNLOADS,dataModel.get(position).getUrl())
        }
        holder.ownerinfo.setOnClickListener{
            val intent=Intent(holder.filename.context,OwnerInfo::class.java)
            val extra=Bundle()
            extra.putString("FileName",dataModel.get(position).getName())
            extra.putString("OwnerName",dataModel.get(position).getOwnerName())
            extra.putString("OwnerEmail",dataModel.get(position).getOwnerEmail())
            extra.putString("BackActivity","ShowFiles")
            intent.putExtras(extra)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            holder.filename.context.startActivity(intent)
        }
    }

    fun downloadFile(
        context: Context,
        fileName: String,
        fileExtension: String,
        destinationDirectory: String?,
        url: String?
    ) {
        val downloadmanager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        Log.d("URL","$url")
        val uri: Uri = Uri.parse(url!!)
        Log.d("URI","$uri")
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS.toString(),fileName)
        downloadmanager.enqueue(request)
        Toast.makeText(context,"Downloading File",Toast.LENGTH_SHORT).show()
    }
    override fun getItemCount(): Int {
        return dataModel.size
    }
}