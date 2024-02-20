package com.hazel.myapplication.ui.fragments.viewer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import com.bumptech.glide.Glide
import com.hazel.myapplication.databinding.FragmentDetailsBinding
import com.hazel.myapplication.ui.viewmodel.SharedViewModel
import com.hazel.myapplication.utils.ExtensionUtils.isImage
import com.hazel.myapplication.utils.OnSwipeTouchListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class DetailsFragment : Fragment() {


    private lateinit var binding: FragmentDetailsBinding
    private lateinit var viewModel: SharedViewModel
    private var position = 0
    private lateinit var currentlist: List<File>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
            androidx.lifecycle.ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        initArgs()
        initClicks()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClicks() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        binding.ivPlayPause.setOnClickListener {
            if (binding.ivVideoPlayer.isPlaying) {
                binding.ivVideoPlayer.pause()
                binding.ivPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                binding.ivVideoPlayer.start()
                binding.ivPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        binding.tvSave.setOnClickListener {
            val file = currentlist[position]
            if (file.isImage()) {
                viewModel.saveImage(position)
            } else {
                viewModel.saveVideo(position)
            }
        }

        binding.ibNext.setOnClickListener {
            nextMedia()
        }

        binding.ibPrevious.setOnClickListener {
            previousMedia()
        }

        binding.ivPreviewImage.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                nextMedia()
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                previousMedia()
            }
        })

    }

    private fun previousMedia() {
        if (position > 0) {
            position--
            loadFile(position)
        } else {
            position = currentlist.size - 1
            loadFile(position)
        }
    }

    private fun nextMedia() {
        if (position < currentlist.size - 1) {
            position++
            loadFile(position)
        } else {
            position = 0
            loadFile(position)
        }
    }

    private fun checkPermission() {
        Dexter.withContext(requireContext())
            .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : com.karumi.dexter.listener.single.PermissionListener {
                override fun onPermissionGranted(p0: com.karumi.dexter.listener.PermissionGrantedResponse?) {
                    if (currentlist[position].isImage()) {
                        viewModel.saveImage(position)
                    } else {
                        viewModel.saveVideo(position)
                    }

                }

                override fun onPermissionDenied(p0: com.karumi.dexter.listener.PermissionDeniedResponse?) {
                    Toast.makeText(
                        requireContext(),
                        "Permission Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()

    }

    private fun initArgs() {
        position = arguments?.getInt("position") ?: 0
        when (arguments?.getString("type") ?: "image") {
            "image" -> {
                currentlist = viewModel.images
            }
            "videos" -> {
                currentlist = viewModel.videos
            }
            "saved" -> {
                binding.tvSave.visibility = View.GONE
                currentlist = viewModel.savedFiles.value
            }
        }
        loadFile(position)
    }

    private fun loadFile(pos: Int) {
        val myFile = currentlist[pos]
        if (myFile.isImage()) {
            loadImages(pos)
        } else {
            loadVideos(pos)
        }
    }

    private fun loadVideos(pos: Int) {
        binding.ivPlayPause.visibility = View.VISIBLE
        binding.ivPreviewImage.visibility = View.GONE
        binding.ivVideoPlayer.visibility = View.VISIBLE
        val myFile = currentlist[pos]
        binding.tvTitle.text = myFile.name
        binding.tvContentNumber.text = "${pos + 1}/${currentlist.size}"

        binding.ivVideoPlayer.setVideoURI(Uri.fromFile(myFile))
        binding.ivVideoPlayer.start()
        binding.ivVideoPlayer.setOnCompletionListener {
            nextMedia()
        }
    }

    private fun loadImages(pos: Int) {
        binding.ivPlayPause.visibility = View.GONE
        binding.ivVideoPlayer.visibility = View.GONE
        binding.ivPreviewImage.visibility = View.VISIBLE
        val myFile = currentlist[pos]
        binding.tvTitle.text = myFile.name
        Glide.with(requireContext()).load(myFile).into(binding.ivPreviewImage)
        binding.tvContentNumber.text = "${pos + 1}/${currentlist.size}"
    }
}

