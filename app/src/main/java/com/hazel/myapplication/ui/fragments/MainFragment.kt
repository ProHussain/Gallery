package com.hazel.myapplication.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.hazel.myapplication.adapter.ViewPagerAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class MainFragment : Fragment() {
    private lateinit var binding: com.hazel.myapplication.databinding.FragmentMainBinding
    private lateinit var adapter: ViewPagerAdapter
    private val androidElevenPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                initTabs()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                showPermissionDeniedDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = com.hazel.myapplication.databinding.FragmentMainBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissions()
        initClickListeners()
    }

    private fun initClickListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentItem = binding.viewPager.currentItem
                if (!adapter.handleBackPress(currentItem)) {
                    if (binding.viewPager.currentItem == 0) {
                        requireActivity().finish()
                    } else {
                        binding.viewPager.setCurrentItem(0, true)
                    }
                }
            }
        })
    }

    private fun initPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                initTabs()
            } else {
                showExternalStoragePermissionDialog()
            }
        } else {
            val permission = arrayListOf<String>()
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                permission.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                permission.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                permission.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            Dexter.withContext(requireActivity())
                .withPermissions(
                    permission
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0?.areAllPermissionsGranted() == true) {
                            initTabs()
                        } else {
                            showPermissionDeniedDialog()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?,
                    ) {
                        p1?.continuePermissionRequest()
                    }


                }).check()

        }


    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showExternalStoragePermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Permission Required")
        builder.setCancelable(false)
        builder.setMessage("Please allow the app to access all files on your device.")
        builder.setPositiveButton("Ok") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent().apply {
                action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            androidElevenPermissionLauncher.launch(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            requireActivity().finish()
        }
        builder.show()
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Permission Required")
        builder.setCancelable(false)
        builder.setMessage("Please grant permissions to use this app.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            initPermissions()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            requireActivity().finish()
        }
        builder.show()
    }

    private fun initTabs() {
        adapter = ViewPagerAdapter(this@MainFragment)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Images"
                1 -> tab.text = "Videos"
                2 -> tab.text = "PDF"
                3 -> tab.text = "Docs"
                4 -> tab.text = "Xlx"
                5 -> tab.text = "PPT"
                6 -> tab.text = "Saved"
            }
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                adapter.handlePageChange(position)
            }
        })

        binding.viewPager.offscreenPageLimit = 6
    }
}