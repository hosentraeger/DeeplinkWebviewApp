package com.example.deeplinkwebviewapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.data.Account

class AccountAdapter(private val accounts: List<Account>) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountNameTextView: TextView = itemView.findViewById(R.id.account_name)
        val accountBalanceTextView: TextView = itemView.findViewById(R.id.account_balance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        holder.accountNameTextView.text = account.name
        holder.accountBalanceTextView.text = account.balance
    }

    override fun getItemCount(): Int = accounts.size
}
