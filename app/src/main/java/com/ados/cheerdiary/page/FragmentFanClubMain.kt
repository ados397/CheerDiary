package com.ados.cheerdiary.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.MyPagerAdapterFanClub
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentFanClubMainBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubMain.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubMain : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubMainBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentFanClubMainBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
        binding.viewpager.apply {
            adapter = MyPagerAdapterFanClub(context as FragmentActivity)
            setPageTransformer(ZoomOutPageTransformer())
        }

        binding.textTabInfo.setOnClickListener {
            binding.viewpager.currentItem = 0
            setTabButton(binding.textTabInfo)
            releaseTabButton(binding.textTabRank)
            releaseTabButton(binding.textTabManagement)
        }
        binding.textTabRank.setOnClickListener {
            binding.viewpager.currentItem = 1
            setTabButton(binding.textTabRank)
            releaseTabButton(binding.textTabInfo)
            releaseTabButton(binding.textTabManagement)
        }
        binding.textTabManagement.setOnClickListener {
            binding.viewpager.currentItem = 2
            setTabButton(binding.textTabManagement)
            releaseTabButton(binding.textTabInfo)
            releaseTabButton(binding.textTabRank)
        }

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun setTabButton(textView: TextView) {
        textView.background = AppCompatResources.getDrawable(requireContext(), R.drawable.btn_round)
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
         * @return A new instance of fragment FragmentFanClubMain.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubMain().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}