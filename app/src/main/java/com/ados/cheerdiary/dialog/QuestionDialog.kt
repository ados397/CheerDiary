package com.ados.cheerdiary.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.QuestionDialogBinding
import com.ados.cheerdiary.model.QuestionDTO

class QuestionDialog(context: Context, var question: QuestionDTO) : Dialog(context), View.OnClickListener {

    lateinit var binding: QuestionDialogBinding

    private val layout = R.layout.question_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textTitle.text = question.title
        binding.textContent.text = question.content

        when(question.stat) {
            QuestionDTO.STAT.INFO -> binding.imgStat.setImageResource(R.drawable.information)
            QuestionDTO.STAT.WARNING -> binding.imgStat.setImageResource(R.drawable.warning)
            QuestionDTO.STAT.ERROR -> binding.imgStat.setImageResource(R.drawable.error)
        }

        if (!question.image.isNullOrEmpty()) {
            var imageID = context.resources.getIdentifier(question.image, "drawable", context.packageName)
            if (imageID != null) {
                binding.imgStat.setImageResource(imageID)
            }
        }
    }

    fun setButtonOk(name: String) {
        binding.buttonQuestionOk.text = name
    }

    fun setButtonCancel(name: String) {
        binding.buttonQuestionCancel.text = name
    }

    fun showButtonOk(visible: Boolean) {
        if (visible == true) {
            binding.buttonQuestionOk.visibility = View.VISIBLE
        } else {
            binding.buttonQuestionOk.visibility = View.GONE
        }
    }

    fun showButtonCancel(visible: Boolean) {
        if (visible == true) {
            binding.buttonQuestionCancel.visibility = View.VISIBLE
        } else {
            binding.buttonQuestionCancel.visibility = View.GONE
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