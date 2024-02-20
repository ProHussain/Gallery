package com.hazel.myapplication.ui.fragments.tabs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import com.hazel.myapplication.R
import com.hazel.myapplication.adapter.FilesPagingAdapter
import com.hazel.myapplication.databinding.FragmentImagesBinding
import com.hazel.myapplication.ui.viewmodel.SharedViewModel
import com.hazel.myapplication.utils.ProgressDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ImagesFragment : Fragment() {
    companion object {
        private var fragment: ImagesFragment? = null
        fun newInstance(): ImagesFragment {
            if (fragment == null) {
                fragment = ImagesFragment()
            }
            return fragment!!
        }
    }
    
    private val TAG = "ImagesFragment"

    private lateinit var binding: FragmentImagesBinding
    private lateinit var viewModel: SharedViewModel
    private lateinit var adapter: FilesPagingAdapter

    private val progressDialog by lazy {
        ProgressDialog(requireContext())
    }

    private var currentPage = 1
    private var isLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initClickListeners()
        initCollectors()
    }

    private fun initClickListeners() {
        binding.tvSelectAll.setOnClickListener {
            adapter.selectAll()
        }

        binding.tvSaveSelection.setOnClickListener {
            if (adapter.selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "No Images Selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressDialog.show()
            progressDialog.updateMessage("Saving Images")
            viewModel.saveSelectedFiles(adapter.selectedItems,"image")
        }
    }

    private fun initAdapter() {
        adapter = FilesPagingAdapter(
            onFileClick = { position, list ->
                val bundle = Bundle()
                bundle.putInt("position", position)
                bundle.putString("type", "image")
                viewModel.images = list
                findNavController().navigate(
                    R.id.action_mainFragment_to_detailsFragment,
                    bundle
                )
            },
            onFileLongClick = {
                binding.tvSelectAll.visibility = View.VISIBLE
                binding.tvSaveSelection.visibility = View.VISIBLE
            }
        )


        binding.rvImages.adapter = adapter
    }

    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.imagesList.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }


        lifecycleScope.launch {
            viewModel.saveImageTask.collectLatest {
                if (it) {
                    progressDialog.dismiss()
                    adapter.clearSelection()
                    binding.tvSelectAll.visibility = View.GONE
                    binding.tvSaveSelection.visibility = View.GONE
                    viewModel.clearProgress()
                    Toast.makeText(requireContext(), "Images Saved", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.progressMessage.collectLatest {
                if (progressDialog.isShowing())
                    progressDialog.updateMessage(it)
            }
        }
    }

    fun handleBackPress(): Boolean {
        Log.e(TAG, "handleBackPress: Called")
        if (adapter.selectionMode) {
            binding.tvSelectAll.visibility = View.GONE
            binding.tvSaveSelection.visibility = View.GONE
            adapter.clearSelection()
            return true
        }
        return false
    }

    fun handlePageChange() {
        if (!this::adapter.isInitialized)
            return@handlePageChange
        if (adapter.selectionMode) {
            binding.tvSelectAll.visibility = View.GONE
            binding.tvSaveSelection.visibility = View.GONE
            adapter.clearSelection()
        }
    }

}