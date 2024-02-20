
package com.hazel.myapplication.ui.fragments.tabs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.hazel.myapplication.R
import com.hazel.myapplication.adapter.FilesAdapter
import com.hazel.myapplication.adapter.FilesPagingAdapter
import com.hazel.myapplication.databinding.FragmentPdfBinding
import com.hazel.myapplication.ui.viewmodel.SharedViewModel
import com.hazel.myapplication.utils.ProgressDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PdfFragment : Fragment() {

    companion object {
        private var fragment: PdfFragment? = null
        fun newInstance(): PdfFragment {
            if (fragment == null) {
                fragment = PdfFragment()
            }
            return fragment!!
        }
    }

    private val TAG = "PdfFragment"

    private lateinit var binding: FragmentPdfBinding
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
        binding = FragmentPdfBinding.inflate(inflater, container, false)
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
            viewModel.saveSelectedFiles(adapter.selectedItems,"pdf")
        }
    }

    private fun initAdapter() {
        adapter = FilesPagingAdapter(
            onFileClick = { position, files ->
                val bundle = Bundle()
                val file = files[position]
                bundle.putString("file", file.path)
                findNavController().navigate(R.id.action_mainFragment_to_pdfViewerFragment, bundle)

            },
            onFileLongClick = {
                binding.tvSelectAll.visibility = View.VISIBLE
                binding.tvSaveSelection.visibility = View.VISIBLE
            }
        )
        binding.rvPdf.adapter = adapter
    }

    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.pdfList.collectLatest {
                Log.e(TAG, "initCollectors: $it")
                adapter.submitData(it)
            }
        }


        lifecycleScope.launch {
            viewModel.savePdfTask.collectLatest {
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