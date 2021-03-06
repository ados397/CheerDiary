package com.ados.cheerdiary.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.EditTextModifyDialogBinding
import com.ados.cheerdiary.model.EditTextDTO
import com.ados.cheerdiary.model.QuestionDTO

class EditTextModifyDialog(context: Context, var item: EditTextDTO) : Dialog(context), View.OnClickListener {

    lateinit var binding: EditTextModifyDialogBinding

    private val layout = R.layout.edit_text_modify_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditTextModifyDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonModifyOk.isEnabled = false

        binding.textTitle.text = item.title
        binding.editContent.setText(item.content)
        if (item.length!! > 0) {
            binding.editContent.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(item.length!!))
            binding.textContentLen.text = "${binding.editContent.text.length}/${item.length}"
        }

        binding.editContent.doAfterTextChanged {
            val content = binding.editContent.text.toString()
            if (item.regex != null) {
                if (!isValid(content)) {
                    binding.textContentError.text = item.regexErrorMsg
                    binding.editContent.setBackgroundResource(R.drawable.edit_rectangle_red)
                } else {
                    binding.textContentError.text = ""
                    binding.editContent.setBackgroundResource(R.drawable.edit_rectangle)
                }
            }

            if (item.content != content) {
                binding.buttonModifyOk.isEnabled = true
                binding.buttonModifyOk.setTextColor(ContextCompat.getColor(context, R.color.text))
            } else {
                binding.buttonModifyOk.isEnabled = false
                binding.buttonModifyOk.setTextColor(ContextCompat.getColor(context, R.color.text_disable))
            }

            binding.textContentLen.text = "${binding.editContent.text.length}/${item.length}"
        }
    }

    private fun isValid(content: String) : Boolean {
        val exp = Regex(item.regex.toString())
        return !content.isNullOrEmpty() && exp.matches(content)
    }

    fun setButtonOk(name: String) {
        binding.buttonModifyOk.text = name
    }

    fun setButtonCancel(name: String) {
        binding.buttonModifyCancel.text = name
    }

    fun showButtonOk(visible: Boolean) {
        if (visible == true) {
            binding.buttonModifyOk.visibility = View.VISIBLE
        } else {
            binding.buttonModifyOk.visibility = View.GONE
        }
    }

    fun showButtonCancel(visible: Boolean) {
        if (visible == true) {
            binding.buttonModifyCancel.visibility = View.VISIBLE
        } else {
            binding.buttonModifyCancel.visibility = View.GONE
        }
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }
}