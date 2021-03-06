package com.ados.cheerdiary.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.MainActivity
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentFanClubMemberBinding
import com.ados.cheerdiary.dialog.QuestionDialog
import com.ados.cheerdiary.model.FanClubDTO
import com.ados.cheerdiary.model.MemberDTO
import com.ados.cheerdiary.model.QuestionDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.question_dialog.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubMember.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubMember : Fragment(), OnFanClubMemberItemClickListener {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentFanClubMemberBinding? = null
    private val binding get() = _binding!!

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubMember

    private var firestore : FirebaseFirestore? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var members : ArrayList<MemberDTO> = arrayListOf()

    private var selectedMember: MemberDTO? = null
    private var selectedPosition: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fanClubDTO = it.getParcelable(ARG_PARAM1)
            currentMember = it.getParcelable(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFanClubMemberBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_fan_club_member!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ?????? ?????? ??????
        binding.layoutMenu.visibility = View.GONE

        setFanClubInfo()

        refresh()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCancel.setOnClickListener {
            selectRecyclerView()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as MainActivity?)?.refreshFanClubDTO { fanClub ->
                fanClubDTO = fanClub

                (activity as MainActivity?)?.refreshMemberDTO { member ->
                    currentMember = member

                    (parentFragment as FragmentFanClubMain?)?.setFanClub(fanClub)
                    (parentFragment as FragmentFanClubMain?)?.setMember(member)
                    (parentFragment as FragmentFanClubMain?)?.setFanClubInfo()

                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(activity, "?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
            refresh()
        }

        binding.buttonDelegateMaster.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.STAT.ERROR,
                "????????? ??????",
                "[${selectedMember?.userNickname}]????????? ???????????? ?????????????????????????\n???????????? ?????????????????? ??????????????? ???????????????."
            )
            val dialog = QuestionDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.button_question_ok.setOnClickListener { // No
                dialog.dismiss()
            }
        }

        binding.buttonAppointSubMaster.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.STAT.INFO,
                "???????????? ??????",
                "[${selectedMember?.userNickname}]?????? ?????????????????? ?????? ???????????????????"
            )
            val dialog = QuestionDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.button_question_ok.setOnClickListener { // Yes
                dialog.dismiss()
                firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())
                    ?.collection("member")?.document(selectedMember?.userUid.toString())?.update("position", MemberDTO.POSITION.SUB_MASTER)?.addOnCompleteListener {
                        members[selectedPosition!!].position = MemberDTO.POSITION.SUB_MASTER
                        recyclerViewAdapter.notifyDataSetChanged()
                        selectRecyclerView()
                        Toast.makeText(activity, "????????? ???????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonFireSubMaster.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.STAT.WARNING,
                "???????????? ??????",
                "[${selectedMember?.userNickname}]?????? ?????????????????? ?????? ???????????????????"
            )
            val dialog = QuestionDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.button_question_ok.setOnClickListener { // Yes
                dialog.dismiss()
                firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())
                    ?.collection("member")?.document(selectedMember?.userUid.toString())?.update("position", MemberDTO.POSITION.MEMBER)?.addOnCompleteListener {
                        members[selectedPosition!!].position = MemberDTO.POSITION.MEMBER
                        recyclerViewAdapter.notifyDataSetChanged()
                        selectRecyclerView()
                        Toast.makeText(activity, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun refresh() {
        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.get()?.addOnCompleteListener { task ->
            members.clear()
            if(task.isSuccessful) {
                for (document in task.result) {
                    var member = document.toObject(MemberDTO::class.java)!!
                    if (member.position != MemberDTO.POSITION.GUEST) {
                        members.add(member)
                    }
                }
                recyclerViewAdapter = RecyclerViewAdapterFanClubMember(members, this)
                recyclerView.adapter = recyclerViewAdapter
            }
        }
    }

    private fun setFanClubInfo() {
        binding.textMemberCount.text = "${fanClubDTO?.count}/${fanClubDTO?.countMax}"

        when {
            isMaster() -> { // ????????? ?????? ?????????
                binding.buttonDelegateMaster.visibility = View.VISIBLE
                binding.buttonAppointSubMaster.visibility = View.VISIBLE
                binding.buttonFireSubMaster.visibility = View.VISIBLE
            }
            isAdministrator() -> { // ???????????? ?????? ?????????
                binding.buttonDelegateMaster.visibility = View.GONE
                binding.buttonAppointSubMaster.visibility = View.GONE
                binding.buttonFireSubMaster.visibility = View.GONE
            }
        }
    }

    private fun isMaster() : Boolean {
        return currentMember?.position == MemberDTO.POSITION.MASTER
    }

    private fun isAdministrator() : Boolean {
        return currentMember?.position == MemberDTO.POSITION.MASTER || currentMember?.position == MemberDTO.POSITION.SUB_MASTER
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubMember.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentFanClubMember().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }

    private fun selectRecyclerView() {
        if (recyclerViewAdapter?.selectItem(selectedPosition!!)) { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ?????????
            if (isAdministrator()) {
                val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
                binding.layoutMenu.visibility = View.VISIBLE
                binding.layoutMenu.startAnimation(translateUp)
                //recyclerView.smoothSnapToPosition(position)
            }
        } else { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ??????
            if (isAdministrator()) {
                val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
                binding.layoutMenu.visibility = View.GONE
                binding.layoutMenu.startAnimation(translateDown)
                //recyclerView.smoothSnapToPosition(position)
            }
        }
    }

    override fun onItemClick(item: MemberDTO, position: Int) {
        val user = (activity as MainActivity?)?.getUser()
        if (user?.uid != item.userUid && item.position != MemberDTO.POSITION.MASTER) {
            selectedMember = item
            selectedPosition = position
            selectRecyclerView()

            if (isMaster()) {
                if (item.position == MemberDTO.POSITION.SUB_MASTER) {
                    binding.buttonAppointSubMaster.visibility = View.GONE
                    binding.buttonFireSubMaster.visibility = View.VISIBLE
                } else {
                    binding.buttonAppointSubMaster.visibility = View.VISIBLE
                    binding.buttonFireSubMaster.visibility = View.GONE
                }
            }
        }
    }
}