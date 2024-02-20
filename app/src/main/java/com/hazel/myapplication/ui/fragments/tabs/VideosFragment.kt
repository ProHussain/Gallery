package com.hazel.myapplication.ui.fragments.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView.*
import com.hazel.myapplication.R
import com.hazel.myapplication.adapter.FilesPagingAdapter
import com.hazel.myapplication.databinding.FragmentVideosBinding
import com.hazel.myapplication.ui.viewmodel.SharedViewModel
import com.hazel.myapplication.utils.ProgressDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class VideosFragment : Fragment() {

    companion object {
        private var fragment: VideosFragment? = null
        fun newInstance(): VideosFragment {
            if (fragment == null) {
                fragment = VideosFragment()
            }
            return fragment!!
        }
    }

    private lateinit var binding: FragmentVideosBinding
    private lateinit var viewModel: SharedViewModel
    private lateinit var adapter: FilesPagingAdapter
    private val progressDialog by lazy {
        ProgressDialog(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentVideosBinding.inflate(inflater, container, false)
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
                Toast.makeText(requireContext(), "Select at least one file", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            progressDialog.show()
            progressDialog.updateMessage("Saving Videos")
            viewModel.saveSelectedFiles(adapter.selectedItems, "videos")
        }
    }

    private fun initAdapter() {
        adapter = FilesPagingAdapter(
            onFileClick = { position, list ->
                val bundle = Bundle()
                bundle.putInt("position", position)
                bundle.putString("type", "image")
                viewModel.videos = list
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

        binding.rvVideos.adapter = adapter
    }

    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.videosList.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            viewModel.saveVideoTask.collectLatest {
                if (it) {
                    progressDialog.dismiss()
                    adapter.clearSelection()
                    binding.tvSelectAll.visibility = View.GONE
                    binding.tvSaveSelection.visibility = View.GONE
                    viewModel.clearProgress()
                    Toast.makeText(requireContext(), "Files Saved", Toast.LENGTH_SHORT).show()
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