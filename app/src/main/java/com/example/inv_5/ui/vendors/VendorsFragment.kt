package com.example.inv_5.ui.vendors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inv_5.databinding.FragmentVendorsBinding

class VendorsFragment : Fragment() {

    private var _binding: FragmentVendorsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vendorsViewModel =
            ViewModelProvider(this).get(VendorsViewModel::class.java)

        _binding = FragmentVendorsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textVendors
        vendorsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}