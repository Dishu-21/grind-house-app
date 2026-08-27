package com.grindhouse.focus

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grindhouse.focus.databinding.ItemAppBinding

class AppListAdapter(private val apps: MutableList<InstalledApp>) :
    RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.appIcon.setImageDrawable(app.icon)
        holder.binding.appLabel.text = app.label
        holder.binding.appCheckbox.setOnCheckedChangeListener(null)
        holder.binding.appCheckbox.isChecked = app.isChecked
        holder.binding.appCheckbox.setOnCheckedChangeListener { _, checked -> app.isChecked = checked }
        holder.binding.root.setOnClickListener { holder.binding.appCheckbox.toggle() }
    }

    override fun getItemCount() = apps.size

    fun selectedPackages(): Set<String> = apps.filter { it.isChecked }.map { it.packageName }.toSet()
}
