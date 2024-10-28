package com.example.deeplinkwebviewapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.data.Account
import com.example.deeplinkwebviewapp.ui.adapter.AccountAdapter

class DashboardFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var accountAdapter: AccountAdapter
    private val accounts = listOf(
        Account("Girokonto", "1.000,00 €"),
        Account("Sparkonto", "5.000,00 €"),
        Account("Depot", "10.000,00 €")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // RecyclerView initialisieren
        recyclerView = view.findViewById(R.id.account_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        accountAdapter = AccountAdapter(accounts)
        recyclerView.adapter = accountAdapter

        return view
    }
}
