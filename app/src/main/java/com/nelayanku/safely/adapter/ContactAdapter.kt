package com.nelayanku.safely.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nelayanku.safely.R
import com.nelayanku.safely.model.Contact

class ContactAdapter(
    private val contactList: List<Contact>,
    val context: Context,
    private val onEditClickListener: (Contact) -> Unit,
    private val onUbahClickListener: (Contact) -> Unit,
    private val onDeleteClickListener: (Contact) -> Unit
    ) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val btnUtama : LinearLayout = itemView.findViewById(R.id.btnUtama)
        val btnEdit : LinearLayout = itemView.findViewById(R.id.btnEdit)
        val lyButton : LinearLayout = itemView.findViewById(R.id.lyButton)
        val btnDelete : LinearLayout = itemView.findViewById(R.id.btnDelete)
        val etUtama : EditText = itemView.findViewById(R.id.etUtama)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentContact = contactList[position]
        holder.tvName.text = currentContact.name
        holder.tvPhone.text = currentContact.phone
        holder.btnUtama.setOnClickListener { onEditClickListener(currentContact) }
        if (currentContact.utama){
            holder.etUtama.setText("Terpilih")
        }
        holder.btnEdit.setOnClickListener {
            onUbahClickListener(currentContact)
        }
        holder.btnDelete.setOnClickListener {
            onDeleteClickListener(currentContact)
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }
}
