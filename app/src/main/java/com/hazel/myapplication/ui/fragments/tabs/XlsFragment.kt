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
import androidx.recyclerview.widget.RecyclerView
import com.hazel.myapplication.R
import com.hazel.myapplication.adapter.FilesAdapter
import com.hazel.myapplication.adapter.FilesPagingAdapter
import com.hazel.myapplication.databinding.FragmentDocsBinding
import com.hazel.myapplication.databinding.FragmentXlsBinding
import com.hazel.myapplication.ui.viewmodel.SharedViewModel
import com.hazel.myapplication.utils.ProgressDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class XlsFragment : Fragment() {

    companion object {
        private var fragment: XlsFragment? = null
        fun newInstance(): XlsFragment {
            if (fragment == null) {
                fragment = XlsFragment()
            }
            return fragment!!
        }
    }

    private val TAG = "XlsFragment"
    private lateinit var binding: FragmentXlsBinding
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
        binding = FragmentXlsBinding.inflate(inflater, container, false)
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
                Toast.makeText(requireContext(), "No Doc Selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressDialog.show()
            progressDialog.updateMessage("Saving Doc")
            viewModel.saveSelectedFiles(adapter.selectedItems, "xls")
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
                intent.setDataAndType(uri, "application/vnd.ms-excel")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            },
            onFileLongClick = {
                binding.tvSelectAll.visibility = View.VISIBLE
                binding.tvSaveSelection.visibility = View.VISIBLE
            }
        )
        binding.rvXls.adapter = adapter
    }

    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.xlsList.collectLatest {
                adapter.submitData(it)
            }
        }


        lifecycleScope.launch {
            viewModel.saveXlsTask.collectLatest {
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