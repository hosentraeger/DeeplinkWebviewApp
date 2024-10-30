package com.example.deeplinkwebviewapp.ui
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.deeplinkwebviewapp.R
import com.example.deeplinkwebviewapp.ui.adapter.ProfileMenuAdapter
import com.example.deeplinkwebviewapp.viewmodel.ProfileViewModel
import androidx.recyclerview.widget.LinearLayoutManager

class ProfileFragment : Fragment() {

    private lateinit var profileMenuAdapter: ProfileMenuAdapter
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel mit Application-Kontext erstellen
        profileViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(ProfileViewModel::class.java)

        val recyclerView: RecyclerView = view.findViewById(R.id.profile_menu_recycler_view)

        profileViewModel.menuItems.observe(viewLifecycleOwner) { menuItems ->
            profileMenuAdapter = ProfileMenuAdapter(menuItems) { selectedItem ->
                profileViewModel.selectMenuItem(selectedItem)
            }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = profileMenuAdapter
            }
        }

        profileViewModel.selectedMenuItem.observe(viewLifecycleOwner) { selectedItem ->
            handleMenuClick(selectedItem)
        }
    }

    private fun handleMenuClick(menuItem: String) {
        when (menuItem) {
            getString(R.string.profile_entry_name_account_settings) -> {
                (activity as? MainActivity)?.showAccountSettings()
            }
            getString(R.string.profile_entry_name_feature_settings) -> {
                (activity as? MainActivity)?.showFeatureSettings()
            }
            getString(R.string.profile_entry_name_system_settings) -> {
                (activity as? MainActivity)?.showSystemSettings()
            }
            getString(R.string.profile_entry_name_notification_settings) -> {
                val intent = Intent(requireContext(), NotificationSettingsActivity::class.java)
                startActivity(intent)
            }
            getString(R.string.profile_entry_name_log) -> {
                val intent = Intent(requireContext(), LogActivity::class.java)
                startActivity(intent)
            }
            getString(R.string.profile_entry_name_logout) -> {
                activity?.finishAffinity()
            }
        }
    }
}
