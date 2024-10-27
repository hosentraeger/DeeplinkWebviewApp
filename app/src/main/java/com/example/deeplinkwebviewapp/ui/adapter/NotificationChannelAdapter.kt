package com.example.deeplinkwebviewapp.ui.adapter

// NotificationChannelAdapter.kt
import android.app.NotificationChannel
import android.app.NotificationManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationChannelAdapter(private val channels: List<NotificationChannel>) :
    RecyclerView.Adapter<NotificationChannelAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = channels[position]
        holder.bind(channel)
    }

    override fun getItemCount() = channels.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(android.R.id.text1)
        private val description: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(channel: NotificationChannel) {
            title.text = channel.name
            description.text = if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                "Nicht abonniert"
            } else {
                "Abonniert"
            }
        }
    }
}
