package com.hazel.myapplication.ui.fragments.viewer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.hazel.myapplication.R
import com.hazel.myapplication.databinding.FragmentPdfViewerBinding
import java.io.File

class PdfViewerFragment : Fragment() {

    private val TAG = "PdfViewerFragment"
    private lateinit var binding: FragmentPdfViewerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        initArgs()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
    }

    private fun initClickListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initArgs() {
        val args = arguments
        val path = args?.getString("file")
        val file = File(path)
        binding.pdfView.fromFile(file).show()
        binding.tvTitle.text = file.name
    }
}