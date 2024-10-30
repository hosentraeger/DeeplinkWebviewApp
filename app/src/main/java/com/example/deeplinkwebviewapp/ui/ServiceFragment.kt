package com.example.deeplinkwebviewapp.ui
// ProfileFragment.kt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.deeplinkwebviewapp.databinding.FragmentServiceBinding
import com.example.deeplinkwebviewapp.viewmodel.ServiceViewModel

class ServiceFragment : Fragment() {

    private var _binding: FragmentServiceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ServiceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceBinding.inflate(inflater, container, false)

        // ViewModel-Observer für die Kontaktdaten
        viewModel.contactName.observe(viewLifecycleOwner, Observer { name ->
            binding.contactName.text = name
        })
        viewModel.contactEmail.observe(viewLifecycleOwner, Observer { email ->
            binding.contactEmail.text = email
        })
        viewModel.contactPhone.observe(viewLifecycleOwner, Observer { phone ->
            binding.contactPhone.text = phone
        })

        // Schieberegler für Aloha, Wero und Cashback
        binding.switchAloha.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAlohaEnabled(isChecked)
        }
        binding.switchWero.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setWeroEnabled(isChecked)
        }
        binding.switchCashback.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setCashbackEnabled(isChecked)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
