package com.ados.cheerdiary.page

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.MainActivity
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentFanClubInfoBinding
import com.ados.cheerdiary.dialog.EditTextModifyDialog
import com.ados.cheerdiary.dialog.QuestionDialog
import com.ados.cheerdiary.dialog.SelectFanClubSymbolDialog
import com.ados.cheerdiary.model.EditTextDTO
import com.ados.cheerdiary.model.FanClubDTO
import com.ados.cheerdiary.model.MemberDTO
import com.ados.cheerdiary.model.QuestionDTO
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.edit_text_modify_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import kotlinx.android.synthetic.main.select_fan_club_symbol_dialog.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubInfo.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubInfo : Fragment(), OnFanClubMemberItemClickListener {
    // TODO: Rename and change types of parameters
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private var _binding: FragmentFanClubInfoBinding? = null
    private val binding get() = _binding!!

    private var firestore : FirebaseFirestore? = null

    //lateinit var recyclerView : RecyclerView
    //lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubMember

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
        _binding = FragmentFanClubInfoBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()

        //recyclerView = rootView.findViewById(R.id.rv_fan_club_member!!)as RecyclerView
        //recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //recyclerViewAdapter = RecyclerViewAdapterFanClubMember(members, this)
        //recyclerView.adapter = recyclerViewAdapter

        //members.add(currentMember!!)
        //recyclerViewAdapter = RecyclerViewAdapterFanClubMember(members, this)
        //recyclerView.adapter = recyclerViewAdapter

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh()

        binding.editNotice.setOnTouchListener { view, motionEvent ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as MainActivity?)?.refreshFanClubDTO { fanClub ->
                fanClubDTO = fanClub

                (activity as MainActivity?)?.refreshMemberDTO { member ->
                    currentMember = member
                    refresh()

                    (parentFragment as FragmentFanClubMain?)?.setFanClub(fanClub)
                    (parentFragment as FragmentFanClubMain?)?.setMember(member)
                    (parentFragment as FragmentFanClubMain?)?.setFanClubInfo()

                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(activity, "?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonModifyImage.setOnClickListener {
            val dialog = SelectFanClubSymbolDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            dialog.setOnDismissListener {
                if (dialog.isOK && !dialog.selectedSymbol.isNullOrEmpty()) {
                    val question = QuestionDTO(
                        QuestionDTO.STAT.WARNING,
                        "????????? ?????? ??????",
                        "????????? ????????? ???????????? ????????? ??? ????????????.\n?????? ?????? ???????????????????",
                        dialog.selectedSymbol
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

                        var imageID = requireContext().resources.getIdentifier(dialog.selectedSymbol, "drawable", requireContext().packageName)
                        if (imageID != null) {
                            fanClubDTO?.imgSymbol = dialog.selectedSymbol
                            firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.set(fanClubDTO!!)?.addOnCompleteListener {
                                Toast.makeText(activity, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                                binding.imgSymbol.setImageResource(imageID)
                            }
                        }
                    }
                }
            }
        }

        binding.buttonModifyNotice.setOnClickListener {
            val item = EditTextDTO("???????????? ??????", fanClubDTO?.notice, 600)
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
                    "???????????? ??????",
                    "??????????????? ???????????? ????????? ??? ????????????.\n?????? ?????? ???????????????????",
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

                    fanClubDTO?.notice = dialog.edit_content.text.toString()
                    firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.set(fanClubDTO!!)?.addOnCompleteListener {
                        Toast.makeText(activity, "???????????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                        binding.editNotice.setText(fanClubDTO?.notice)
                    }
                }
            }
        }
    }

    private fun refresh() {
        setFanClubInfo()
        setCurrentMemberInfo()
    }

    private fun setCurrentMemberInfo() {
        when (currentMember?.position) {
            MemberDTO.POSITION.MASTER -> binding.layoutPosition.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.master))
            MemberDTO.POSITION.SUB_MASTER -> binding.layoutPosition.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.sub_master))
            MemberDTO.POSITION.MEMBER -> binding.layoutPosition.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.member))
        }
        binding.imgPosition.setImageResource(currentMember?.getPositionImage()!!)
        binding.textPosition.text = currentMember?.getPositionString()

        binding.textMemberLevel.text = "Lv. ${currentMember?.userLevel}"
        binding.textMemberName.text = currentMember?.userNickname
        binding.textContribution.text = "????????? : ${decimalFormat.format(currentMember?.contribution)}"
        binding.textResponseTime.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(currentMember?.responseTime)
        binding.textAboutMe.text = currentMember?.userAboutMe
        binding.imgCheckout.setImageResource(currentMember?.getCheckoutImage()!!)
    }

    private fun setFanClubInfo() {
        var imageID = requireContext().resources.getIdentifier(fanClubDTO?.imgSymbol, "drawable", requireContext().packageName)
        binding.imgSymbol.setImageResource(imageID)
        binding.textLevel.text = "Lv. ${fanClubDTO?.level}"
        binding.textName.text = fanClubDTO?.name
        binding.editNotice.setText(fanClubDTO?.notice)

        when {
            isMaster() -> { // ????????? ?????? ?????????
                binding.buttonModifyImage.visibility = View.VISIBLE
                binding.buttonModifyNotice.visibility = View.VISIBLE
            }
            isAdministrator() -> { // ???????????? ?????? ?????????
                binding.buttonModifyImage.visibility = View.GONE
                binding.buttonModifyNotice.visibility = View.VISIBLE
            }
            else -> {
                binding.buttonModifyImage.visibility = View.GONE
                binding.buttonModifyNotice.visibility = View.GONE
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
         * @return A new instance of fragment FragmentFanClubInfo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentFanClubInfo().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: MemberDTO, position: Int) {

    }
}