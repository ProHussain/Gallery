package com.hazel.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy.ALL
import com.hazel.myapplication.R
import com.hazel.myapplication.databinding.LayoutFileItemBinding
import com.hazel.myapplication.utils.ExtensionUtils.isDoc
import com.hazel.myapplication.utils.ExtensionUtils.isPdf
import com.hazel.myapplication.utils.ExtensionUtils.isPpt
import com.hazel.myapplication.utils.ExtensionUtils.isVideo
import com.hazel.myapplication.utils.ExtensionUtils.isXls
import java.io.File

class FilesAdapter (
    private val onFileClick : (Int) -> Unit,
    private val onFileLongClick : (Int) -> Unit = {}
) : ListAdapter<File,FilesAdapter.ViewHolder>(FileDiffUtilCallback) {

    var selectionMode = false
    val selectedItems = arrayListOf<File>()
    val selectedPositions = arrayListOf<Int>()

    object FileDiffUtilCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.name == newItem.name
        }
    }

    inner class ViewHolder(private val binding: LayoutFileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBindData(myFile: File) {
            binding.tvFile.text = myFile.name


            when {
                myFile.isPdf() -> {
                    binding.ivFile.setImageResource(R.drawable.ic_pdf)
                }
                myFile.isDoc() -> {
                    binding.ivFile.setImageResource(R.drawable.ic_doc)
                }
                myFile.isXls() -> {
                    binding.ivFile.setImageResource(R.drawable.ic_xls)
                }
                myFile.isPpt() -> {
                    binding.ivFile.setImageResource(R.drawable.ic_ppt)
                }
                else -> {
                    Glide.with(binding.root.context).load(myFile.absolutePath)
                        .thumbnail(0.1f)
                        .diskCacheStrategy(ALL)
                        .into(binding.ivFile)
                }
            }

            if (myFile.isVideo()) {
                binding.ivPlay.visibility = ViewGroup.VISIBLE
            } else {
                binding.ivPlay.visibility = ViewGroup.GONE
            }

            if (selectedPositions.contains(bindingAdapterPosition)) {
                binding.root.setBackgroundColor(binding.root.context.resources.getColor(android.R.color.holo_green_light,null))
            } else {
                binding.root.setBackgroundColor(binding.root.context.resources.getColor(android.R.color.white,null))
            }

            binding.root.setOnClickListener {
                if (selectionMode) {
                    if (selectedItems.contains(myFile)) {
                        selectedItems.remove(myFile)
                        selectedPositions.remove(bindingAdapterPosition)
                        binding.root.setBackgroundColor(binding.root.context.resources.getColor(android.R.color.white,null))
                    } else {
                        selectedItems.add(myFile)
                        selectedPositions.add(bindingAdapterPosition)
                        binding.root.setBackgroundColor(binding.root.context.resources.getColor(android.R.color.holo_green_light,null))
                    }
                } else {
                    onFileClick(bindingAdapterPosition)
                }
            }

            binding.root.setOnLongClickListener {
                if (!selectionMode) {
                    selectionMode = true
                    selectedItems.add(myFile)
                    selectedPositions.add(bindingAdapterPosition)
                    binding.root.setBackgroundColor(binding.root.context.resources.getColor(android.R.color.holo_green_light,null))
                    onFileLongClick(bindingAdapterPosition)
                }
                true
            }
        }

    }

    

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutFileItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindData(getItem(position))
    }

    fun  clearSelection() {
        selectionMode = false
        selectedItems.clear()
        selectedPositions.clear()
        notifyDataSetChanged()
    }

    fun selectAll() {
        if (selectedItems.size == currentList.size) {
            selectedItems.clear()
            selectedPositions.clear()
        } else {
            selectedItems.clear()
            selectedPositions.clear()
            selectedItems.addAll(currentList)
            selectedPositions.addAll(currentList.indices)
        }
        notifyDataSetChanged()
    }
}
