package com.example.deeplinkwebviewapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.deeplinkwebviewapp.R

class ChooseInstitionBottomSheet(private val items: Array<String>, private val targetUri: String, private val isSilentLogin: Boolean ) : BottomSheetDialogFragment() {

    interface OnChoiceSelectedListener {
        fun onChoiceSelected(choice: String?, targetUri: String, isSilentLogin: Boolean)
    }

    private lateinit var listener: OnChoiceSelectedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Stelle sicher, dass die Aktivität die Schnittstelle implementiert
        if (context is OnChoiceSelectedListener) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnChoiceSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_institution, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)

        val listView = view.findViewById<ListView>(R.id.listView)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            // Hier wird die Auswahl ausgewertet
            val selectedChoice = items[position]
            listener.onChoiceSelected(selectedChoice, targetUri, isSilentLogin) // Rückgabe der Auswahl an die Aktivität
            dismiss() // Schließt das Bottom Sheet
        }
    }
}
