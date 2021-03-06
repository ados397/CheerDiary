package com.ados.cheerdiary.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.MainActivity
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentFanClubManagementBinding
import com.ados.cheerdiary.dialog.EditTextModifyDialog
import com.ados.cheerdiary.dialog.QuestionDialog
import com.ados.cheerdiary.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.edit_text_modify_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubManagement.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubManagement : Fragment(), OnFanClubSignUpItemClickListener {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentFanClubManagementBinding? = null
    private val binding get() = _binding!!

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubSignUp

    private var firestore : FirebaseFirestore? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var members : ArrayList<MemberDTO> = arrayListOf()

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
        _binding = FragmentFanClubManagementBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_fan_club_sign_up!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ????????? ?????? ??????
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

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as MainActivity?)?.refreshFanClubDTO { fanClub ->
                fanClubDTO = fanClub

                (activity as MainActivity?)?.refreshMemberDTO { member ->
                    currentMember = member

                    (parentFragment as FragmentFanClubMain?)?.setFanClub(fanClub)
                    (parentFragment as FragmentFanClubMain?)?.setMember(member)
                    (parentFragment as FragmentFanClubMain?)?.setFanClubInfo()

                    setFanClubInfo()
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(activity, "?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
            refresh()
        }

        binding.buttonModifyDescription.setOnClickListener {
            val item = EditTextDTO("????????? ?????? ??????", fanClubDTO?.description, 600)
            val dialog = EditTextModifyDialog(requireContext(), item)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_modify_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.button_modify_ok.setOnClickListener {
                dialog.dismiss()

                val question = QuestionDTO(
                    QuestionDTO.STAT.WARNING,
                    "????????? ?????? ??????",
                    "????????? ?????? ???????????? ????????? ??? ????????????.\n?????? ?????? ???????????????????",
                )
                val questionDialog = QuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_question_ok.setOnClickListener {
                    questionDialog.dismiss()

                    fanClubDTO?.description = dialog.edit_content.text.toString()
                    firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.set(fanClubDTO!!)?.addOnCompleteListener {
                        Toast.makeText(activity, "????????? ?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                        binding.editDescription.setText(fanClubDTO?.description)
                    }
                }
            }
        }

        binding.buttonApproval.setOnClickListener {
            hideMenu()
            for (member in members) {
                if (member.isSelected) {
                    member.isSelected = false
                    member.position = MemberDTO.POSITION.MEMBER
                    member.responseTime = Date()

                    // ?????? ?????? ????????? ????????? ???????????? ????????? ??????
                    firestore?.collection("user")?.document(member?.userUid.toString())?.get()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (task.result.exists()) { // document ??????
                                var user = task.result.toObject(UserDTO::class.java)!!
                                if (!user.fanClubId.isNullOrEmpty()) {
                                    Toast.makeText(activity, "?????? ?????? ???????????? ????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
                                    members.remove(member)
                                    refresh()
                                } else {
                                    // ????????? ????????? ??????
                                    firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.document(member.userUid.toString())?.set(member)?.addOnCompleteListener {
                                        // ????????? ?????? ?????? ??? ?????????(count) ??? 1 ??????
                                        var tsDoc = firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())
                                        firestore?.runTransaction { transaction ->
                                            val fanClub = transaction.get(tsDoc!!).toObject(FanClubDTO::class.java)
                                            fanClub?.count = fanClub?.count?.plus(1)
                                            transaction.set(tsDoc, fanClub!!)
                                        }?.addOnSuccessListener { result ->

                                        }?.addOnFailureListener { e ->

                                        }
                                    }

                                    // ????????? ????????? ????????? ID ??????
                                    user.fanClubId = fanClubDTO?.docName
                                    firestore?.collection("user")?.document(member.userUid.toString())?.update("fanClubId", user.fanClubId.toString())?.addOnCompleteListener {
                                        (activity as MainActivity?)?.setUser(user)
                                    }

                                    // ?????? ??? ????????? ????????? ????????? ????????? ????????? ??????
                                    firestore?.collection("fanClub")?.get()?.addOnCompleteListener { task ->
                                        if(task.isSuccessful) {
                                            for (document in task.result) {
                                                var fanClub = document.toObject(FanClubDTO::class.java)!!
                                                if (fanClubDTO?.docName != fanClub.docName) { // ?????? ???????????? ?????? ????????? ???????????? ??????
                                                    var deleteDocument = firestore?.collection("fanClub")?.document(fanClub.docName.toString())?.collection("member")?.document(member?.userUid.toString())
                                                    deleteDocument?.get()?.addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            if (task.result.exists()) { // document ?????? - ?????? ??????
                                                                deleteDocument?.delete()?.addOnCompleteListener {
                                                                    // ?????? ??????
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Toast.makeText(activity, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                                    members.remove(member)
                                    refresh()
                                }
                            }
                        }
                    }
                }
            }

        }

        binding.buttonReject.setOnClickListener {
            hideMenu()
            for (member in members) {
                if (member.isSelected) {
                    firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.document(member.userUid.toString())?.delete()?.addOnCompleteListener {

                    }
                }
            }
            refresh()
            Toast.makeText(activity, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
        }

        binding.buttonFanClubQuit.setOnClickListener {
            if (currentMember?.position == MemberDTO.POSITION.MASTER) {
                val question = QuestionDTO(
                    QuestionDTO.STAT.ERROR,
                    "????????? ??????",
                    "???????????? ????????? ????????? ??? ??? ????????????. ???????????? ????????? ??? ????????? ???????????????.",
                )
                val questionDialog = QuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.setButtonCancel("??????")
                questionDialog.showButtonOk(false)
                questionDialog.button_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
            } else {
                val question = QuestionDTO(
                    QuestionDTO.STAT.ERROR,
                    "????????? ??????",
                    "????????? ?????? ??? ????????? ??? ?????? ????????? ????????? ?????????. ?????? ?????? ???????????????????",
                )
                val questionDialog = QuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_question_ok.setOnClickListener {
                    questionDialog.dismiss()

                    // ????????? ???????????? ??????
                    firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.document(currentMember?.userUid.toString())?.delete()?.addOnCompleteListener {
                        // ????????? ????????? 0????????? ????????? ??????
                        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.document(currentMember?.userUid.toString())?.delete()?.addOnCompleteListener {

                        }
                        // user ???????????? ????????? ?????? ??????
                        var user = (activity as MainActivity?)?.getUser()
                        user?.fanClubId = null
                        firestore?.collection("user")?.document(user?.uid.toString())?.set(user!!)?.addOnCompleteListener {
                            // ????????? ??????/?????? ???????????? ??????
                            (activity as MainActivity?)?.setUser(user)
                        }
                    }
                }
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
         * @return A new instance of fragment FragmentFanClubManagement.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentFanClubManagement().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }

    private fun setFanClubInfo() {
        binding.editDescription.setText(fanClubDTO?.description)

        when {
            isAdministrator() -> { // ?????????, ???????????? ?????? ?????????
                binding.buttonModifyDescription.visibility = View.VISIBLE
                binding.layoutSignUp.visibility = View.VISIBLE
                binding.editDescription.maxLines = 4
            }
            else -> {
                binding.buttonModifyDescription.visibility = View.GONE
                binding.layoutSignUp.visibility = View.GONE
                binding.editDescription.maxLines = 10
            }
        }
    }

    private fun refresh() {
        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.get()?.addOnCompleteListener { task ->
            members.clear()
            if(task.isSuccessful) {
                for (document in task.result) {
                    var member = document.toObject(MemberDTO::class.java)!!
                    if (member.position == MemberDTO.POSITION.GUEST) {
                        members.add(member)
                    }
                }
                recyclerViewAdapter = RecyclerViewAdapterFanClubSignUp(members, this)
                recyclerView.adapter = recyclerViewAdapter
            }
        }
    }

    private fun isMaster() : Boolean {
        return currentMember?.position == MemberDTO.POSITION.MASTER
    }

    private fun isAdministrator() : Boolean {
        return currentMember?.position == MemberDTO.POSITION.MASTER || currentMember?.position == MemberDTO.POSITION.SUB_MASTER
    }

    private fun hideMenu() {
        val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenu.startAnimation(translateDown)
        //recyclerView.smoothSnapToPosition(position)
    }

    override fun onItemClick(item: MemberDTO, position: Int) {
        if (recyclerViewAdapter.isChecked()) { // ????????? ????????? ???????????? ????????? ??????
            if (!binding.layoutMenu.isVisible) { // ?????? ???????????? ?????? ?????? ?????? ?????? ???????????? ??????
                val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
                binding.layoutMenu.visibility = View.VISIBLE
                binding.layoutMenu.startAnimation(translateUp)
                //recyclerView.smoothSnapToPosition(position)
            }
        } else { // ?????? ?????? ???????????? ?????? ??????
            hideMenu()
        }
    }
}