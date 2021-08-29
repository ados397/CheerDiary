package com.ados.cheerdiary.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.CalendarDialogBinding
import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener
import com.applikeysolutions.cosmocalendar.selection.RangeSelectionManager
import com.applikeysolutions.cosmocalendar.utils.SelectionType

class CalendarDialog(context: Context, private val isRange: Boolean = true) : Dialog(context), View.OnClickListener {

    lateinit var binding: CalendarDialogBinding

    private val layout = R.layout.calendar_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CalendarDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)

        if (isRange) {
            binding.calendarView.selectionType = SelectionType.RANGE
            binding.calendarView.selectionManager = RangeSelectionManager(OnDaySelectedListener {
                /*println("========== setSelectionManager ==========")
                println("Selected Dates : " + binding.calendarView.selectedDates.size)
                if (binding.calendarView.selectedDates.size <= 0) return@OnDaySelectedListener
                println("Selected Days : " + binding.calendarView.selectedDays)
                println("Start Day : " + binding.calendarView.selectedDays[0])
                println("End Day : " + binding.calendarView.selectedDays[binding.calendarView.selectedDays.lastIndex])*/
            })
        } else {
            binding.calendarView.selectionType = SelectionType.SINGLE
        }


    }

    fun setButtonOk(name: String) {
        //binding.buttonOk.text = name
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