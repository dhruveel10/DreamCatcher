package edu.vt.cs5254.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.room.DeleteColumn
import edu.vt.cs5254.dreamcatcher.databinding.ActivityMainBinding
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamBinding
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamEntryBinding

class DreamEntryHolder(private val binding: ListItemDreamEntryBinding): RecyclerView.ViewHolder(binding.root){
    lateinit var boundEntry: DreamEntry
        private set

    fun bind(entry: DreamEntry){
        boundEntry = entry
        binding.entryButton.configureForEntry(entry)
    }

    private fun Button.configureForEntry(entry: DreamEntry){
        visibility = View.VISIBLE
        text = entry.kind.toString()
        when(entry.kind){
            DreamEntryKind.REFLECTION -> {
                setBackgroundWithContrastingText("#3AABD8")
                isAllCaps = false
                text = entry.text
            }

            DreamEntryKind.CONCEIVED -> {
                setBackgroundWithContrastingText("#8CC4DA")
            }

            DreamEntryKind.DEFERRED -> {
                setBackgroundWithContrastingText("black")
            }

            DreamEntryKind.FULFILLED ->{
                setBackgroundWithContrastingText("#B0D0F2")
            }


        }
    }
}

class DreamEntryAdapter(private val entries: List<DreamEntry>): RecyclerView.Adapter<DreamEntryHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamEntryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamEntryBinding.inflate(inflater, parent, false)
        return  DreamEntryHolder(binding)
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
        holder.bind(entries[position])
    }
}