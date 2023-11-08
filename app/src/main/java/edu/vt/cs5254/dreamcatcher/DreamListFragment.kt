package edu.vt.cs5254.dreamcatcher

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamListBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DreamListFragment: Fragment() {

    private var _binding: FragmentDreamListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {"FragmentDreamDetailBinding is null"}

    private val vm: DreamListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDreamListBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_dream_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return  when(menuItem.itemId){
                    R.id.new_dream ->{
                        Log.w("---DLF---", "Menu item New Dream clicked")
                        showNewDream()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getItemTouchHelper().attachToRecyclerView(binding.dreamRecyclerView)

        binding.dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.dreams.collect {dreams ->
                    if (dreams.isNotEmpty()) {
                        binding.noDreamAddButton.visibility = View.GONE
                        binding.noDreamText.visibility = View.GONE
                    }
                    else {
                        binding.noDreamAddButton.visibility = View.VISIBLE
                        binding.noDreamText.visibility = View.VISIBLE
                    }

                    binding.dreamRecyclerView.adapter = DreamListAdapter(dreams){dreamId ->
                        Log.w("---DLF---", "Clicked Dream ID: $dreamId")
                        findNavController().navigate(DreamListFragmentDirections.showDreamDetail(dreamId))
                    }
                }
            }
        }

        binding.noDreamAddButton.setOnClickListener {
            showNewDream()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showNewDream(){

        viewLifecycleOwner.lifecycleScope.launch {
            val dream = Dream()
            vm.insertDream(dream)
            findNavController().navigate(DreamListFragmentDirections.showDreamDetail(dream.id))
        }

    }

    private fun getItemTouchHelper(): ItemTouchHelper{
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.w("---DLF---", "Swipe left detected")
                val dreamHolder = viewHolder as DreamHolder
                vm.deleteDream(dreamHolder.boundDream)
            }

        })
    }
}