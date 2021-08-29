package com.ados.cheerdiary.page

import android.app.ActionBar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentDashboardMissionBinding
import com.ados.cheerdiary.model.ScheduleDTO
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import com.ados.cheerdiary.R.drawable.btn_round
import com.ados.cheerdiary.dialog.MissionDialog
import com.ados.cheerdiary.dialog.SelectAppDialog
import kotlinx.android.synthetic.main.select_app_dialog.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDashboardMission.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDashboardMission : Fragment(), OnMissionItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentDashboardMissionBinding? = null
    private val binding get() = _binding!!

    lateinit var recyclerViewClub : RecyclerView
    lateinit var recyclerViewClubAdapter : RecyclerViewAdapterMission
    lateinit var recyclerViewPersonal : RecyclerView
    lateinit var recyclerViewPersonalAdapter : RecyclerViewAdapterMission

    private var isExpandClub: Boolean = true
    private var isExpandPersonal: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardMissionBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView
        recyclerViewClub = rootView.findViewById(R.id.rv_mission_club!!)as RecyclerView
        recyclerViewClub.layoutManager = LinearLayoutManager(requireContext())

        var datas : ArrayList<ScheduleDTO> = arrayListOf()
        datas.apply {
            for (i in 0..100) {
                add(ScheduleDTO(false, "뉴스 댓글 총공"))
                add(ScheduleDTO(false, "리매치 2000만 달성"))
                add(ScheduleDTO(false, "생일 하트 적립"))
            }
        }
        recyclerViewClubAdapter = RecyclerViewAdapterMission(datas, this)
        recyclerViewClub.adapter = recyclerViewClubAdapter


        recyclerViewPersonal = rootView.findViewById(R.id.rv_mission_personal!!)as RecyclerView
        recyclerViewPersonal.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewPersonalAdapter = RecyclerViewAdapterMission(datas, this)
        recyclerViewPersonal.adapter = recyclerViewPersonalAdapter

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonHideClub.setOnClickListener {
            if (isExpandClub) {
                isExpandClub = false


                //binding.layoutClub.startAnimation(translateUp)
                //binding.layoutPersonal.startAnimation(translateUp)

                binding.buttonHideClub.setImageResource(R.drawable.expend)
                binding.rvMissionClub.visibility = View.GONE
                (binding.rvMissionPersonal.layoutParams as LinearLayout.LayoutParams).weight = 90F

                //val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
                //binding.rvMissionClub.startAnimation(translateUp)

                //(binding.layoutClub.layoutParams as LinearLayout.LayoutParams).weight = 0.5F
                //(binding.layoutTitleClub.layoutParams as LinearLayout.LayoutParams).weight = 10F
                //(binding.layoutPersonal.layoutParams as LinearLayout.LayoutParams).weight = 9.5F



            } else {
                isExpandClub = true

                //binding.layoutClub.startAnimation(translateDown)
                //binding.layoutPersonal.startAnimation(translateDown)

                binding.buttonHideClub.setImageResource(R.drawable.minimize)
                binding.rvMissionClub.visibility = View.VISIBLE
                (binding.rvMissionPersonal.layoutParams as LinearLayout.LayoutParams).weight = 45F

                //val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
                //binding.rvMissionClub.startAnimation(translateDown)

                //(binding.layoutClub.layoutParams as LinearLayout.LayoutParams).weight = 5F
                //(binding.layoutTitleClub.layoutParams as LinearLayout.LayoutParams).weight = 1F
                //(binding.layoutPersonal.layoutParams as LinearLayout.LayoutParams).weight = 5F
            }
        }

        binding.buttonHidePersonal.setOnClickListener {
            if (isExpandPersonal) {
                isExpandPersonal = false

                binding.buttonHidePersonal.setImageResource(R.drawable.expend)
                binding.rvMissionPersonal.visibility = View.GONE
                (binding.rvMissionClub.layoutParams as LinearLayout.LayoutParams).weight = 90F

                //(binding.layoutPersonal.layoutParams as LinearLayout.LayoutParams).weight = 0.5F
                //(binding.layoutTitlePersonal.layoutParams as LinearLayout.LayoutParams).weight = 10F
                //(binding.layoutClub.layoutParams as LinearLayout.LayoutParams).weight = 9.5F
            } else {
                isExpandPersonal = true

                binding.buttonHidePersonal.setImageResource(R.drawable.minimize)
                binding.rvMissionPersonal.visibility = View.VISIBLE
                (binding.rvMissionClub.layoutParams as LinearLayout.LayoutParams).weight = 45F

                //(binding.layoutPersonal.layoutParams as LinearLayout.LayoutParams).weight = 5F
                //(binding.layoutTitlePersonal.layoutParams as LinearLayout.LayoutParams).weight = 1F
                //(binding.layoutClub.layoutParams as LinearLayout.LayoutParams).weight = 5F
            }
        }

        binding.textTabDay.setOnClickListener {
            setTabButton(binding.textTabDay)
            releaseTabButton(binding.textTabWeek)
            releaseTabButton(binding.textTabMonth)
            releaseTabButton(binding.textTabPeriod)
        }
        binding.textTabWeek.setOnClickListener {
            setTabButton(binding.textTabWeek)
            releaseTabButton(binding.textTabDay)
            releaseTabButton(binding.textTabMonth)
            releaseTabButton(binding.textTabPeriod)
        }
        binding.textTabMonth.setOnClickListener {
            setTabButton(binding.textTabMonth)
            releaseTabButton(binding.textTabDay)
            releaseTabButton(binding.textTabWeek)
            releaseTabButton(binding.textTabPeriod)
        }
        binding.textTabPeriod.setOnClickListener {
            setTabButton(binding.textTabPeriod)
            releaseTabButton(binding.textTabDay)
            releaseTabButton(binding.textTabWeek)
            releaseTabButton(binding.textTabMonth)
        }
    }

    private fun setTabButton(textView: TextView) {
        textView.background = getDrawable(requireContext(), btn_round)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun releaseTabButton(textView: TextView) {
        textView.background = null
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentDashboardMission.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentDashboardMission().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: ScheduleDTO, position: Int) {
        val dialog = MissionDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        dialog.button_cancel.setOnClickListener { // No
            dialog.dismiss()
        }

        dialog.button_ok.setOnClickListener { // Ok

            dialog.dismiss()
        }
    }
}