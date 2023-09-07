package com.nelayanku.safely.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nelayanku.safely.R
import com.nelayanku.safely.model.Jadwal

class JadwalAdapter(
    private val jadwalList: List<Jadwal>,
    private val context: Context
) : RecyclerView.Adapter<JadwalAdapter.JadwalViewHolder>() {

    class JadwalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvWaktu: TextView = itemView.findViewById(R.id.tvWaktu)
        val tvDurasi: TextView = itemView.findViewById(R.id.tvDurasi)
        val tvMode: TextView = itemView.findViewById(R.id.tvMode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jadwal, parent, false)
        return JadwalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
        val currentJadwal = jadwalList[position]
        holder.tvTanggal.text = currentJadwal.tanggal
        holder.tvWaktu.text = "Pukul "+currentJadwal.waktu
        holder.tvDurasi.text = currentJadwal.durasi + " menit"
        holder.tvMode.text = "Kamera "+currentJadwal.mode

    }

    override fun getItemCount(): Int {
        return jadwalList.size
    }
}
