package com.hazel.myapplication.ui.fragments.tabs

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hazel.myapplication.R
import com.hazel.myapplication.adapter.FilesAdapter
import com.hazel.myapplication.databinding.FragmentSavedBinding
import com.hazel.myapplication.ui.viewmodel.SharedViewModel
import com.hazel.myapplication.utils.ExtensionUtils.isDoc
import com.hazel.myapplication.utils.ExtensionUtils.isImage
import com.hazel.myapplication.utils.ExtensionUtils.isPdf
import com.hazel.myapplication.utils.ExtensionUtils.isPpt
import com.hazel.myapplication.utils.ExtensionUtils.isVideo
import com.hazel.myapplication.utils.ExtensionUtils.isXls
import com.hazel.myapplication.utils.ProgressDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class SavedFragment : Fragment() {

    companion object {
        private var fragment: SavedFragment? = null
        fun newInstance(): SavedFragment {
            if (fragment == null) {
                fragment = SavedFragment()
            }
            return fragment!!
        }
    }

    private lateinit var binding: FragmentSavedBinding
    private lateinit var viewModel: SharedViewModel
    private lateinit var adapter: FilesAdapter
    private val progressDialog by lazy {
        ProgressDialog(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel= androidx.lifecycle.ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initClickListeners()
        initCollectors()
    }

    override fun onResume() {
        super.onResume()
        viewModel.readSavedFiles()
    }

    private fun initClickListeners() {
        binding.tvSelectAll.setOnClickListener {
            adapter.selectAll()
        }

        binding.tvDeleteSelection.setOnClickListener {
            if (adapter.selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Select Files to Delete", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            progressDialog.show()
            progressDialog.updateMessage("Deleting Files...")
            viewModel.deleteFiles(adapter.selectedItems)
        }
    }


    private fun initAdapter() {
        adapter = FilesAdapter(
            onFileClick = {
                val file = adapter.currentList[it]
                if (file.isImage() || file.isVideo()) {
                    val bundle = Bundle()
                    bundle.putInt("position", it)
                    bundle.putString("type", "saved")
                    findNavController().navigate(R.id.action_mainFragment_to_detailsFragment, bundle)
                } else {
                    if (file.isPdf()) {
                        val bundle = Bundle()
                        val file = adapter.currentList[it]
                        bundle.putString("file", file.path)
                        findNavController().navigate(R.id.action_mainFragment_to_pdfViewerFragment, bundle)
                    } else {
                        openFile(file)
                    }
                }

            },
            onFileLongClick = {
                binding.tvSelectAll.visibility = View.VISIBLE
                binding.tvDeleteSelection.visibility = View.VISIBLE
            }
        )
        binding.rvSaved.adapter = adapter
    }

    private fun openFile(file: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().applicationContext.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        if (file.isDoc())
            intent.setDataAndType(uri, "application/msword")
        else if (file.isXls())
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        else if (file.isPpt())
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.savedFiles.collect(){
                adapter.submitList(it)
            }
        }

        lifecycleScope.launch {
            viewModel.deleteTask.collect(){
                if (it){
                    progressDialog.dismiss()
                    adapter.clearSelection()
                    binding.tvSelectAll.visibility = View.GONE
                    binding.tvDeleteSelection.visibility = View.GONE
                    Toast.makeText(requireContext(), "Files Deleted", Toast.LENGTH_SHORT).show()
                    viewModel.clearProgress()
                    viewModel.readSavedFiles()
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
            binding.tvDeleteSelection.visibility = View.GONE
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
            binding.tvDeleteSelection.visibility = View.GONE
            adapter.clearSelection()
        }
    }
}