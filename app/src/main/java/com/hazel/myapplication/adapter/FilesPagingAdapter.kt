package com.hazel.myapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hazel.myapplication.R
import com.hazel.myapplication.databinding.LayoutFileItemBinding
import com.hazel.myapplication.utils.ExtensionUtils.isDoc
import com.hazel.myapplication.utils.ExtensionUtils.isPdf
import com.hazel.myapplication.utils.ExtensionUtils.isPpt
import com.hazel.myapplication.utils.ExtensionUtils.isVideo
import com.hazel.myapplication.utils.ExtensionUtils.isXls
import java.io.File

class FilesPagingAdapter(
    private val onFileClick: (Int, List<File>) -> Unit,
    private val onFileLongClick: (Int) -> Unit = {},
) : PagingDataAdapter<File, FilesPagingAdapter.ViewHolder>(FileDiffUtilCallback) {

    var selectionMode = false
    val selectedItems = arrayListOf<File>()
    val selectedPositions = arrayListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutFileItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.onBindData(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.checkFileSelection(getItem(position)!!)
        }
    }

    fun clearSelection() {
        selectionMode = false
        selectedItems.clear()
        selectedPositions.clear()
    }

    fun selectAll() {
        val currentList = snapshot().items
        if (selectedItems.size == currentList.size) {
            selectedItems.clear()
            selectedPositions.clear()
            notifyItemRangeChanged(0, currentList.size, "Unselected")
        } else {
            selectedItems.clear()
            selectedPositions.clear()
            selectedItems.addAll(currentList)
            selectedPositions.addAll(currentList.indices)
            notifyItemRangeChanged(0, currentList.size, "Selected")
        }
    }

    inner class ViewHolder(private val binding: LayoutFileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

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
                    try {
                        myFile.let {
                            Log.d("FilesPagingAdapter", "onBindData: ${myFile.absolutePath}")
                        }
                        Glide.with(binding.root.context).load(myFile.absolutePath)
                            .thumbnail(0.1f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.ic_pdf)
                            .into(binding.ivFile)
                    } catch (e: Exception) {
                        Log.e("FilesPagingAdapter", "onBindData: ${e.message}")
                    }

                }
            }

            if (myFile.isVideo()) {
                binding.ivPlay.visibility = ViewGroup.VISIBLE
            } else {
                binding.ivPlay.visibility = ViewGroup.GONE
            }

            checkFileSelection(myFile)

            binding.root.setOnClickListener {
                if (selectionMode) {
                    if (selectedItems.contains(myFile)) {
                        selectedItems.remove(myFile)
                        selectedPositions.remove(absoluteAdapterPosition)
                        notifyItemChanged(absoluteAdapterPosition, "Unselected")
                    } else {
                        selectedItems.add(myFile)
                        selectedPositions.add(absoluteAdapterPosition)
                        notifyItemChanged(absoluteAdapterPosition, "Selected")
                    }
                    checkFileSelection(myFile)
                } else {
                    onFileClick(absoluteAdapterPosition, snapshot().items)
                }
            }

            binding.root.setOnLongClickListener {
                onItemLongClicked(myFile)
                true
            }
        }

        private fun onItemLongClicked(myFile: File) {
            if (!selectionMode) {
                selectionMode = true
                selectedItems.add(myFile)
                selectedPositions.add(absoluteAdapterPosition)
                onFileLongClick(absoluteAdapterPosition)
            } else {
                if (selectedItems.contains(myFile)) {
                    selectedItems.remove(myFile)
                    selectedPositions.remove(absoluteAdapterPosition)
                    notifyItemChanged(absoluteAdapterPosition, "Unselected")
                } else {
                    selectedItems.add(myFile)
                    selectedPositions.add(absoluteAdapterPosition)
                    notifyItemChanged(absoluteAdapterPosition, "Selected")
                }
            }
            checkFileSelection(myFile)
        }

        internal fun checkFileSelection(myFile: File) {
            if (selectedPositions.contains(absoluteAdapterPosition)) {
                binding.root.setBackgroundColor(
                    binding.root.context.resources.getColor(
                        android.R.color.holo_green_light,
                        null
                    )
                )
            } else {
                binding.root.setBackgroundColor(
                    binding.root.context.resources.getColor(
                        android.R.color.white,
                        null
                    )
                )
            }
        }
    }

    object FileDiffUtilCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.name == newItem.name
        }
    }
}
