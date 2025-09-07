package com.example.swapit1.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swapit1.R
import com.example.swapit1.model.Notification

class NotificationAdapter(private val notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNotification: TextView = itemView.findViewById(R.id.notification_text)
        val iconRes : ImageView = itemView.findViewById(R.id.notification_icon)
        val time : TextView = itemView.findViewById(R.id.notification_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notifications[position]
        holder.iconRes.setImageResource(item.iconRes)
        holder.textNotification.text = item.message
        holder.time.text = item.timeText

        if (position == notifications.size - 1) {
            holder.itemView.findViewById<View>(R.id.divider).visibility = View.GONE
        } else {
            holder.itemView.findViewById<View>(R.id.divider).visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = notifications.size
}

