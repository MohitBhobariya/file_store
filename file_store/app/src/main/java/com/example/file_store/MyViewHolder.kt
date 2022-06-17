package com.example.file_store

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val filename: TextView =itemView.findViewById<TextView>(R.id.name_of_file)
    val download=itemView.findViewById<View>(R.id.download_button)



}