package edu.vt.cs5254.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamListBinding
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamBinding
import java.util.UUID


class DreamHolder(private val binding: ListItemDreamBinding): RecyclerView.ViewHolder(binding.root){

    lateinit var boundDream: Dream
        private set

    fun bind(dream:Dream, onDreamClicked: (dreamId:UUID) -> Unit){
        boundDream = dream
        binding.root.setOnClickListener {
            onDreamClicked(dream.id)
        }

        binding.listItemTitle.text = dream.title

        binding.listItemReflectionCount.text =
            binding.root.context.getString(
                R.string.reflection_count,
                dream.entries.count{
                    it.kind == DreamEntryKind.REFLECTION
                }
            );

        with(binding.listItemImage) {
            when {
                dream.isFulfilled -> {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_dream_fulfilled)
                }

                dream.isDeferred -> {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_dream_deferred)
                }

                else -> {
                    visibility = View.GONE
                }
            }
        }
     }
}


class DreamListAdapter(private val dreams: List<Dream>, private val onDreamClicked: (dreamId: UUID) -> Unit)
    : RecyclerView.Adapter<DreamHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder{
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamBinding.inflate(inflater, parent, false)
        return DreamHolder(binding)
    }

    override fun getItemCount(): Int {
        return dreams.size
    }

    override fun onBindViewHolder(holder: DreamHolder, position: Int) {
        holder.bind(dreams[position], onDreamClicked)
    }

}
