package edu.vt.cs5254.dreamcatcher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class DreamDetailFragment: Fragment(){
    private var _binding: FragmentDreamDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding){"FragmentDetailBinding is Null"}

    private val vm: DreamDetailViewModel by viewModels(){
        DreamDetailViewModelFactory(args.dreamId)
    }

    private val args: DreamDetailFragmentArgs by navArgs()

    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ){tookPicture ->
        Log.w("---DDF---", "Took picture")
        if(tookPicture){
            binding.dreamPhoto.tag = null
            vm.dream.value?.let {
                updatePhoto(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDreamDetailBinding.inflate(inflater, container,false)

        requireActivity().addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_dream_detail, menu)
                menu.findItem(R.id.take_photo_menu).isVisible = canResolve(
                    photoLauncher.contract.createIntent(
                        requireContext(),
                        Uri.EMPTY
                    )
                )
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId){
                    R.id.share_dream_menu ->{
                        val reportIntent = Intent(Intent.ACTION_SEND).apply {
                            var dreamVal = vm.dream.value?.let { shareDream(it) }
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, dreamVal)
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                getString(R.string.share_dream)
                            )
                        }
                        val chooserIntent = Intent.createChooser(
                            reportIntent,
                            getString(R.string.share_dream)
                        )
                        startActivity(chooserIntent)
                        true
                    }
                    R.id.take_photo_menu -> {
                        Log.w("---DDF--", "Take photo menu clicked")

                        vm.dream.value?.let {
                            val photoFile = File(
                                requireActivity().filesDir,
                                it.photoFileName
                            )

                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "edu.vt.cs5254.dreamcatcher.fileprovider",
                                photoFile
                            )

                            photoLauncher.launch(photoUri)
                        }

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

        Log.w("---DLF---", "Received arg ${args.dreamId}")

        getItemTouchHelper().attachToRecyclerView(binding.dreamEntryRecycler)

        binding.dreamEntryRecycler.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.dream.collect{dream ->
                    dream?.let {
                        binding.dreamEntryRecycler.adapter = DreamEntryAdapter(it.entries)
                        updateView(it)
                    }
                }
            }

        }

        binding.titleText.doOnTextChanged{text, _, _, _ ->
            vm.updateDream { oldDream ->
                val newDream = oldDream.copy(title = text.toString())
                    .apply { entries = oldDream.entries }
                newDream
            }
        }

        binding.deferredCheckbox.setOnClickListener {
            vm.updateDream { oldDream ->
                oldDream.copy()
                    .apply { entries =
                        if(oldDream.isDeferred){
                            oldDream.entries.filter { it.kind != DreamEntryKind.DEFERRED }
                        }else{
                            oldDream.entries + DreamEntry(kind = DreamEntryKind.DEFERRED, dreamId = id)
                        }
                    }
            }
        }

        binding.fulfilledCheckbox.setOnClickListener {
            vm.updateDream { oldDream ->
                oldDream.copy()
                    .apply { entries =
                        if(oldDream.isFulfilled){
                            oldDream.entries.filter { it.kind != DreamEntryKind.FULFILLED }
                        }else{
                            oldDream.entries + DreamEntry(kind = DreamEntryKind.FULFILLED, dreamId = id)
                        }
                    }
            }
        }

        binding.addReflectionButton.setOnClickListener {
            findNavController().navigate(
                DreamDetailFragmentDirections.addReflection()
            )
        }

        setFragmentResultListener(ReflectionDialogFragment.REQUEST_KEY){_, bundle ->
            val text = bundle.getString(ReflectionDialogFragment.BUNDLE_KEY) ?: ""
            vm.updateDream { oldDream->
                oldDream.copy().apply {
                    entries = oldDream.entries + DreamEntry(kind = DreamEntryKind.REFLECTION, text = text, dreamId = oldDream.id)
                }
            }
        }

        binding.dreamPhoto.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    vm.dream.collect {
                        if (it != null) {
                            findNavController().navigate(
                                DreamDetailFragmentDirections.showPhotoDetail(it.photoFileName))
                        }
                    }
                }
            }
        }

    }

    private fun updateView(dream: Dream) {

        binding.apply {
            if(titleText.text.toString() != dream.title){
                titleText.setText(dream.title)
            }
        }

        val lastUpdateDate = DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss a", dream.lastUpdated)
        binding.lastUpdatedText.text = getString(R.string.last_updated, lastUpdateDate)



        binding.deferredCheckbox.isChecked = dream.isDeferred
        binding.fulfilledCheckbox.isChecked = dream.isFulfilled

        binding.deferredCheckbox.isEnabled = !dream.isFulfilled
        binding.fulfilledCheckbox.isEnabled = !dream.isDeferred

        if(dream.isFulfilled)
            binding.addReflectionButton?.hide()
        else
            binding.addReflectionButton?.show()

        updatePhoto(dream)
    }

    private fun updatePhoto(dream: Dream){
        Log.w("---DDF---", "Photo updated")

        if (binding.dreamPhoto.tag != dream.photoFileName) {

            val photoFile = File(
                requireActivity().filesDir,
                dream.photoFileName
            )

            if (photoFile.exists()) {
                Log.w("---DDF---", "Photo exists")
                binding.dreamPhoto.doOnLayout { imgView ->
                    val bitmap = getScaledBitmap(
                        photoFile.path,
                        imgView.width,
                        imgView.height
                    )
                    binding.dreamPhoto.setImageBitmap(bitmap)
                    binding.dreamPhoto.tag = dream.photoFileName
                    binding.dreamPhoto.isEnabled = true
                }
            } else {
                Log.w("---DDF---", "Photo doesn't exists")
                binding.dreamPhoto.setImageBitmap(null)
                binding.dreamPhoto.tag = null
                binding.dreamPhoto.isEnabled = false
            }
        }
    }



    private fun canResolve(intent: Intent): Boolean{
        return requireActivity().packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun getItemTouchHelper(): ItemTouchHelper{
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deHolder = viewHolder as DreamEntryHolder
                val entryToDelete =  deHolder.boundEntry
                vm.updateDream { oldDream ->
                    oldDream.copy()
                        .apply { entries = oldDream.entries.filterNot { it.id == entryToDelete.id } }
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val deHolder = viewHolder as DreamEntryHolder
                val entryToSwipe =  deHolder.boundEntry
                return  if(entryToSwipe.kind == DreamEntryKind.REFLECTION){
                    ItemTouchHelper.LEFT
                } else{
                    0
                }
            }

        })
    }

    private fun shareDream(dream: Dream): String{
        val dateFormatted = DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss a", dream.lastUpdated)
        val lastUpdated = getString(R.string.last_updated, dateFormatted)

        var status = ""

        var reflectionsString = ""
        dream.entries.filter { it.kind == DreamEntryKind.REFLECTION }.forEach{
            reflectionsString += "\n * "+ it.text
        }

        if (dream.isDeferred) {
            status = getString(R.string.share_dream_status, "Deferred")
        }
        else if (dream.isFulfilled) {
            status = getString(R.string.share_dream_status, "Fulfilled")
        }

        if (reflectionsString.isEmpty()) {
            return getString(R.string.share_dream, dream.title, lastUpdated, "", status)
        }
        else {
            return getString(R.string.share_dream, dream.title, lastUpdated, "Reflections:$reflectionsString", status)
        }
    }
}