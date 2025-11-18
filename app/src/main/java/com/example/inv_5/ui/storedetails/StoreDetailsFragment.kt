package com.example.inv_5.ui.storedetails

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.inv_5.MainActivity
import com.example.inv_5.R
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.StoreDetails
import com.example.inv_5.databinding.FragmentStoreDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreDetailsFragment : Fragment() {

    private var _binding: FragmentStoreDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveStoreDetails.setOnClickListener { validateAndConfirm() }
        observeExistingDetails()
    }

    private fun observeExistingDetails() {
        lifecycleScope.launch {
            val storedDetails = withContext(Dispatchers.IO) {
                DatabaseProvider.getInstance(requireContext()).storeDetailsDao().getStoreDetails()
            }
            if (storedDetails != null) {
                showSavedState(storedDetails)
            } else {
                showFormState()
            }
        }
    }

    private fun showFormState() {
        binding.formContainer.visibility = View.VISIBLE
        binding.savedContainer.visibility = View.GONE
    }

    private fun showSavedState(details: StoreDetails) {
        binding.formContainer.visibility = View.GONE
        binding.savedContainer.visibility = View.VISIBLE

        binding.textStoreNameValue.text = formatValue(R.string.store_details_store_name_label, details.storeName)
        binding.textCaptionValue.text = formatValue(R.string.store_details_caption_label, details.caption)
        binding.textAddressValue.text = formatValue(R.string.store_details_address_label, details.address)
        binding.textPhoneValue.text = formatValue(R.string.store_details_phone_label, details.phone)
        binding.textOwnerValue.text = formatValue(R.string.store_details_owner_label, details.owner)
    }

    private fun validateAndConfirm() {
        val storeName = binding.inputStoreName.text?.toString()?.trim().orEmpty()
        val caption = binding.inputCaption.text?.toString()?.trim().orEmpty()
        val address = binding.inputAddress.text?.toString()?.trim().orEmpty()
        val phone = binding.inputPhone.text?.toString()?.trim().orEmpty()
        val owner = binding.inputOwner.text?.toString()?.trim().orEmpty()

        var isValid = true
        binding.inputStoreNameLayout.error = null
        binding.inputAddressLayout.error = null
        binding.inputPhoneLayout.error = null

        if (storeName.isEmpty()) {
            binding.inputStoreNameLayout.error = getString(R.string.error_store_name_required)
            isValid = false
        }
        if (address.isEmpty()) {
            binding.inputAddressLayout.error = getString(R.string.error_address_required)
            isValid = false
        }
        if (phone.isEmpty()) {
            binding.inputPhoneLayout.error = getString(R.string.error_phone_required)
            isValid = false
        } else if (phone.length != 10) {
            binding.inputPhoneLayout.error = getString(R.string.error_phone_length)
            isValid = false
        }

        if (!isValid) {
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.store_details_confirmation_title)
            .setMessage(R.string.store_details_confirmation_message)
            .setPositiveButton(R.string.action_save_store_details) { _, _ ->
                persistStoreDetails(storeName, caption, address, phone, owner)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun persistStoreDetails(
        storeName: String,
        caption: String,
        address: String,
        phone: String,
        owner: String
    ) {
        binding.btnSaveStoreDetails.isEnabled = false
        binding.progressSave.isVisible = true

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val dao = DatabaseProvider.getInstance(requireContext()).storeDetailsDao()
                if (dao.hasStoreDetails()) {
                    return@withContext false
                }
                dao.insert(
                    StoreDetails(
                        storeName = storeName,
                        caption = caption.ifBlank { null },
                        address = address,
                        phone = phone,
                        owner = owner.ifBlank { null }
                    )
                )
                true
            }

            binding.progressSave.isVisible = false
            binding.btnSaveStoreDetails.isEnabled = true

            if (!isAdded) {
                return@launch
            }

            if (result) {
                Toast.makeText(requireContext(), getString(R.string.store_details_saved_toast), Toast.LENGTH_LONG).show()
                showSavedState(
                    StoreDetails(
                        storeName = storeName,
                        caption = caption.ifBlank { null },
                        address = address,
                        phone = phone,
                        owner = owner.ifBlank { null }
                    )
                )
                (activity as? MainActivity)?.refreshStoreDetailsMenuState()
            } else {
                Toast.makeText(requireContext(), getString(R.string.store_details_exists_message), Toast.LENGTH_LONG).show()
                observeExistingDetails()
            }
        }
    }

    private fun formatValue(@StringRes labelRes: Int, value: String?): String {
        val finalValue = if (value.isNullOrBlank()) {
            getString(R.string.store_details_not_available)
        } else {
            value
        }
        return getString(R.string.store_details_value_format, getString(labelRes), finalValue)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
