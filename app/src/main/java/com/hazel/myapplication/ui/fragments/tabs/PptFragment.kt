package com.hazel.myapplication.ui.fragments.tabs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.hazel.myapplication.R
import com.hazel.myapplication.adapter.FilesAdapter
import com.hazel.myapplication.adapter.FilesPagingAdapter
import com.hazel.myapplication.databinding.FragmentPdfBinding
import com.hazel.myapplication.databinding.FragmentPptBinding
import com.hazel.myapplication.ui.viewmodel.SharedViewModel
import com.hazel.myapplication.utils.ProgressDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class PptFragment : Fragment() {

    companion object {
        private var fragment: PptFragment? = null
        fun newInstance(): PptFragment {
            if (fragment == null) {
                fragment = PptFragment()
            }
            return fragment!!
        }
    }

    private val TAG = "PptFragment"

    private lateinit var binding: FragmentPptBinding
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
        binding = FragmentPptBinding.inflate(inflater, container, false)
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
                Toast.makeText(requireContext(), "No PDF Selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressDialog.show()
            progressDialog.updateMessage("Saving PDF")
            viewModel.saveSelectedFiles(adapter.selectedItems, "pdf")
        }
    }

    private fun initAdapter() {
        adapter = FilesPagingAdapter(
            onFileClick = { position, list ->
                val file = list[position]
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().applicationContext.packageName + ".provider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            },
            onFileLongClick = {
                binding.tvSelectAll.visibility = View.VISIBLE
                binding.tvSaveSelection.visibility = View.VISIBLE
            }
        )
        binding.rvPpt.adapter = adapter
    }

    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.pptList.collectLatest {
                adapter.submitData(it)
            }
        }


        lifecycleScope.launch {
            viewModel.savePptTask.collectLatest {
                if (it) {
                    progressDialog.dismiss()
                    adapter.clearSelection()
                    binding.tvSelectAll.visibility = View.GONE
                    binding.tvSaveSelection.visibility = View.GONE
                    viewModel.clearProgress()
                    Toast.makeText(requireContext(), "PDF Saved", Toast.LENGTH_SHORT).show()
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
        Log.e(TAG, "handlePageChange: Called")

        if (!this::adapter.isInitialized)
            return@handlePageChange

        if (adapter.selectionMode) {
            binding.tvSelectAll.visibility = View.GONE
            binding.tvSaveSelection.visibility = View.GONE
            adapter.clearSelection()
        }
    }
}