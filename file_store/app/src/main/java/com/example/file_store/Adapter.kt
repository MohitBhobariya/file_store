package com.example.file_store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class Adapter(val showFiles: ShowFiles, val dataModel: ArrayList<DataModel>) :
    RecyclerView.Adapter<MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater:LayoutInflater= LayoutInflater.from(showFiles.baseContext)
        val view:View = layoutInflater.inflate(R.layout.elements,null,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.filename.setText(dataModel.get(position).get())
    }

    override fun getItemCount(): Int {
        return dataModel.size
    }
}