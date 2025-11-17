package com.example.inv_5.ui.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R

class BluetoothDeviceAdapter(
    private val onConnectClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    private val devices = mutableListOf<BluetoothDevice>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceNameText: TextView = view.findViewById(R.id.deviceNameText)
        val deviceAddressText: TextView = view.findViewById(R.id.deviceAddressText)
        val connectButton: Button = view.findViewById(R.id.connectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        
        holder.deviceNameText.text = device.name ?: "Unknown Device"
        holder.deviceAddressText.text = device.address
        
        holder.connectButton.setOnClickListener {
            onConnectClick(device)
        }
        
        // Make the whole card clickable
        holder.itemView.setOnClickListener {
            onConnectClick(device)
        }
    }

    override fun getItemCount(): Int = devices.size

    @SuppressLint("NotifyDataSetChanged")
    fun setDevices(newDevices: List<BluetoothDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    fun clear() {
        devices.clear()
        notifyDataSetChanged()
    }
}
