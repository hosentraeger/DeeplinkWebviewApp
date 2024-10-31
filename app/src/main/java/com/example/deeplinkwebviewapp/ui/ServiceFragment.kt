package com.example.deeplinkwebviewapp.ui
// ProfileFragment.kt
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.deeplinkwebviewapp.databinding.FragmentServiceBinding
import com.example.deeplinkwebviewapp.viewmodel.ServiceViewModel
import com.example.deeplinkwebviewapp.viewmodel.ServiceViewModelFactory

class ServiceFragment : Fragment() {

    private var _binding: FragmentServiceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ServiceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceBinding.inflate(inflater, container, false)

        // ViewModel mit Factory instanziieren
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val factory = ServiceViewModelFactory(requireActivity().application, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(ServiceViewModel::class.java)

        // Beobachten der URLs und Intent starten
        viewModel.serviceCenterUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                val intent = Intent(requireContext(), WebViewActivity::class.java).apply {
                    putExtra("EXTRA_URL", url)
                    putExtra("IF_SILENT_LOGIN", true)
                }
                startActivity(intent)
            }
        }

        viewModel.deeplinksUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                val intent = Intent(requireContext(), WebViewActivity::class.java).apply {
                    putExtra("EXTRA_URL", url)
                    putExtra("IF_SILENT_LOGIN", false)
                }
                startActivity(intent)
            }
        }

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

        // Button-Klick-Listener
        binding.serviceCenterButton.setOnClickListener {
            onServiceCenterClicked()
        }

        // Button-Klick-Listener
        binding.deeplinksButton.setOnClickListener {
            onDeeplinksClicked()
        }

        return binding.root
    }

    private fun onServiceCenterClicked() {
        viewModel.handleServiceCenterClick()
    }

    private fun onDeeplinksClicked() {
        // Führe die gewünschte Aktion hier aus, zum Beispiel eine ViewModel-Funktion aufrufen
        viewModel.handleDeeplinksClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
