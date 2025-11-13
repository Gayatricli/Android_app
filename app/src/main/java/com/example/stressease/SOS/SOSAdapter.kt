package com.example.stressease.SOS

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.Api.CrisisContact
import com.example.stressease.R

class SOSAdapter: RecyclerView.Adapter<SOSAdapter.ViewHolder>(){

    private var contacts: List<CrisisContact> = emptyList()

    fun setData(list: List<CrisisContact>?) {
        contacts = list.orEmpty()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sos_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.number.text = contact.number ?: "Online Resource"
        holder.description.text = contact.description

        holder.itemView.setOnClickListener {
            contact.number?.let {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it"))
                holder.itemView.context.startActivity(dialIntent)
            } ?: contact.website?.let {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                holder.itemView.context.startActivity(webIntent)
            }
        }
    }

    override fun getItemCount() = contacts.size
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvSOSName)
        val number: TextView = itemView.findViewById(R.id.tvSOSNumber)
        val description: TextView = itemView.findViewById(R.id.tvSOSDescription)
    }
}

