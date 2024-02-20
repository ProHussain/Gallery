package com.hazel.myapplication.utils

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import com.hazel.myapplication.R
import com.hazel.myapplication.databinding.LayoutProgressDialogBinding

class ProgressDialog(context: Context) {
    private var dialog: Dialog = Dialog(context)
    private var binding : LayoutProgressDialogBinding = LayoutProgressDialogBinding.inflate(dialog.layoutInflater)

    init {
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun updateMessage(message: String) {
        binding.tvProgressDialogMessage.text = message
    }

    fun isShowing(): Boolean {
        return dialog.isShowing
    }

}