package com.nelayanku.safely.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nelayanku.safely.R
import com.nelayanku.safely.model.MediaItem

class MediaListAdapter(
    private val mediaList: List<MediaItem>,
    //klik tombol gdrive
    private val onGDriveClick: (MediaItem) -> Unit,
    //klik tombol delete
    private val onDeleteClick: (MediaItem) -> Unit,
    private val onACtionClick: (MediaItem) -> Unit,
    ) :
    RecyclerView.Adapter<MediaListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tvName)
        val durationTextView: TextView = view.findViewById(R.id.tvDurasi)
        val sizeTextView: TextView = view.findViewById(R.id.tvUkuran)
        val dateTextView: TextView = view.findViewById(R.id.tvTangggal)
        val imgCover: ImageView = view.findViewById(R.id.imgCover)
        val btnGDrive: LinearLayout = view.findViewById(R.id.btnGDrive)
        val btnDelete: LinearLayout = view.findViewById(R.id.btnDelete)
        val btnAction: LinearLayout = view.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        holder.nameTextView.text = mediaItem.name
        holder.durationTextView.text = mediaItem.duration
        holder.sizeTextView.text = mediaItem.size
        holder.dateTextView.text = mediaItem.date
        //ada jpg, ogg, dan mp4
        if (mediaItem.type == "jpg") {
            holder.durationTextView.visibility = View.GONE
        } else {
            holder.durationTextView.visibility = View.VISIBLE
        }
        //ubah img cover berdasarkan type
        when (mediaItem.type) {
            "jpg" -> Glide.with(holder.itemView.context).load(R.drawable.ic_image_placeholder).into(holder.imgCover)
            "ogg" -> Glide.with(holder.itemView.context).load(R.drawable.ic_sound_placeholder).into(holder.imgCover)
            "mp4" -> Glide.with(holder.itemView.context).load(R.drawable.ic_video_placeholder).into(holder.imgCover)
        }
        //klik tombol gdrive
        holder.btnGDrive.setOnClickListener {
            onGDriveClick(mediaItem)
        }
        //klik tombol delete
        holder.btnDelete.setOnClickListener {
            onDeleteClick(mediaItem)
        }
        //klik tombol action
        holder.btnAction.setOnClickListener {
            onACtionClick(mediaItem)
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }
}
