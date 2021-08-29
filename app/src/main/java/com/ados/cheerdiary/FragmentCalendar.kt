package com.ados.cheerdiary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.ados.cheerdiary.databinding.FragmentCalendarBinding
import com.ados.cheerdiary.page.FragmentScheduleAdd
import com.ados.cheerdiary.page.FragmentScheduleList
import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener
import com.applikeysolutions.cosmocalendar.selection.RangeSelectionManager
import com.applikeysolutions.cosmocalendar.utils.SelectionType
import kotlinx.android.synthetic.main.calendar_dialog.*
import java.text.SimpleDateFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentCalendar.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentCalendar : Fragment() {
    // TODO: Rename and change types of parameters
    private var isRange: Boolean? = true
    private var param2: String? = null

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isRange = it.getBoolean(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isRange == true) {
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

        binding.buttonOk.setOnClickListener {

            var startDate = SimpleDateFormat("yyyy-MM-dd").format(binding.calendarView.selectedDays[0].calendar.time)
            var endDate = SimpleDateFormat("yyyy-MM-dd").format(binding.calendarView.selectedDays[binding.calendarView.selectedDays.lastIndex].calendar.time)

            val fragment = FragmentScheduleAdd.newInstance(startDate, endDate)
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonCancel.setOnClickListener {
            val fragment = FragmentScheduleAdd()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                addToBackStack(null)
                commit()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentCalendar.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Boolean, param2: String) =
            FragmentCalendar().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}