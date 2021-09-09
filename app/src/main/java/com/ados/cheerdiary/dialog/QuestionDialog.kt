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
    }

    fun setButtonOk(name: String) {
        binding.buttonOk.text = name
    }

    fun setButtonCancel(name: String) {
        binding.buttonCancel.text = name
    }

    fun showButtonOk(visible: Boolean) {
        if (visible == true) {
            binding.buttonOk.visibility = View.VISIBLE
        } else {
            binding.buttonOk.visibility = View.GONE
        }
    }

    fun showButtonCancel(visible: Boolean) {
        if (visible == true) {
            binding.buttonCancel.visibility = View.VISIBLE
        } else {
            binding.buttonCancel.visibility = View.GONE
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