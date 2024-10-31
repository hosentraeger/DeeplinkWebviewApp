package com.example.deeplinkwebviewapp.ui.adapter
import com.example.deeplinkwebviewapp.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileMenuAdapter(
    private val menuItems: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProfileMenuAdapter.ProfileMenuViewHolder>() {

    inner class ProfileMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.menu_item_text)

        init {
            itemView.setOnClickListener {
                // Verwende 'bindingAdapterPosition' anstelle von 'adapterPosition'
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) { // Überprüfen, ob die Position gültig ist
                    val item = menuItems[position]
                    onItemClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileMenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_menu, parent, false)
        return ProfileMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileMenuViewHolder, position: Int) {
        holder.textView.text = menuItems[position]
    }

    override fun getItemCount(): Int = menuItems.size
}
