package com.ados.cheerdiary.dialog


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.MissionDialogBinding
import com.ados.cheerdiary.model.DashboardMissionDTO
import com.ados.cheerdiary.model.ScheduleDTO
import java.text.SimpleDateFormat


class MissionDialog(context: Context) : Dialog(context), View.OnClickListener {

    lateinit var binding: MissionDialogBinding

    private val layout = R.layout.mission_dialog

    var dashboardMissionDTO: DashboardMissionDTO? = null

    var missionCount: Long = 0L
    var missionCountMax: Long = 0L
    var missionPercent: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MissionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.textTitle.text = dashboardMissionDTO?.scheduleDTO?.title
        binding.editPurpose.setText(dashboardMissionDTO?.scheduleDTO?.purpose)
        binding.textRange.text = "${SimpleDateFormat("yyyy.MM.dd").format(dashboardMissionDTO?.scheduleDTO?.startDate)} ~ ${SimpleDateFormat("yyyy.MM.dd").format(dashboardMissionDTO?.scheduleDTO?.endDate)}"

        missionCount = dashboardMissionDTO?.scheduleProgressDTO?.count!!
        missionCountMax = dashboardMissionDTO?.scheduleDTO?.count!!

        binding.textRate.text = "${missionCount}/${missionCountMax}"
        getPercent()

        when (dashboardMissionDTO?.scheduleDTO?.action) { // 앱 실행
            ScheduleDTO.ACTION.APP -> {
                binding.textExecute.text = "앱 실행"
                binding.imgIcon.setImageResource(R.drawable.app)
                binding.textAppName.visibility = View.VISIBLE
                binding.textAppName.text = "[${dashboardMissionDTO?.scheduleDTO?.appDTO?.appName}]"
            }
            ScheduleDTO.ACTION.URL -> {
                binding.textExecute.text = "링크 실행"
                binding.imgIcon.setImageResource(R.drawable.link)
                binding.textAppName.visibility = View.GONE
            }
        }

        binding.buttonMinus100.setOnClickListener {
            if (missionCount > 0) {
                if (missionCount - 100 < 0) {
                    missionCount = 0
                } else {
                    missionCount = missionCount.minus(100)
                }
                println("미션 카운트 $missionCount")
                binding.textRate.text = "${missionCount}/${missionCountMax}"

                getPercent()
            }
        }
        binding.buttonMinus10.setOnClickListener {
            if (missionCount > 0) {
                if (missionCount - 10 < 0) {
                    missionCount = 0
                } else {
                    missionCount = missionCount.minus(10)
                }
                println("미션 카운트 $missionCount")
                binding.textRate.text = "${missionCount}/${missionCountMax}"

                getPercent()
            }
        }
        binding.buttonMinus.setOnClickListener {
            if (missionCount > 0) {
                missionCount--
                binding.textRate.text = "${missionCount}/${missionCountMax}"

                getPercent()
            }
        }
        binding.buttonPlus.setOnClickListener {
            if (missionCount < missionCountMax) {
                missionCount++
                binding.textRate.text = "${missionCount}/${missionCountMax}"

                getPercent()
            }
        }
        binding.buttonPlus10.setOnClickListener {
            if (missionCount < missionCountMax) {
                if (missionCount + 10 > missionCountMax) {
                    missionCount = missionCountMax
                } else {
                    missionCount = missionCount.plus(10)
                }
                println("미션 카운트 $missionCount")
                binding.textRate.text = "${missionCount}/${missionCountMax}"

                getPercent()
            }
        }
        binding.buttonPlus100.setOnClickListener {
            if (missionCount < missionCountMax) {
                if (missionCount + 100 > missionCountMax) {
                    missionCount = missionCountMax
                } else {
                    missionCount = missionCount.plus(100)
                }
                println("미션 카운트 $missionCount")
                binding.textRate.text = "${missionCount}/${missionCountMax}"

                getPercent()
            }
        }
        binding.buttonMax.setOnClickListener {
            missionCount = missionCountMax
            binding.textRate.text = "${missionCount}/${missionCountMax}"

            getPercent()
        }

        binding.buttonExecute.setOnClickListener {
            when (dashboardMissionDTO?.scheduleDTO?.action) { // 앱 실행
                ScheduleDTO.ACTION.APP -> {
                    val linePackage = dashboardMissionDTO?.scheduleDTO?.appDTO?.packageName.toString()
                    val intentLine = context.packageManager.getLaunchIntentForPackage(linePackage) // 인텐트에 패키지 주소 저장

                    try {
                        context.startActivity(intentLine) // 라인 앱을 실행해본다.
                    } catch (e: Exception) {  // 만약 실행이 안된다면 (앱이 없다면)
                        val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$linePackage")) // 설치 링크를 인텐트에 담아
                        context.startActivity(intentPlayStore) // 플레이스토어로 이동
                    }
                }
                ScheduleDTO.ACTION.URL -> {
                    val address = dashboardMissionDTO?.scheduleDTO?.url
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(address))
                    context.startActivity(intent)
                }
            }
        }


        binding.editPurpose.setOnTouchListener { view, motionEvent ->
            //binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun getPercent() {
        missionPercent = ((missionCount.toDouble() / missionCountMax) * 100).toInt()
        binding.progressPercent.progress = missionPercent

        binding.textPercent.text = "${missionPercent}%"

        if (missionPercent < 100) {
            binding.imgComplete.visibility = View.GONE
            when {
                missionPercent < 40 -> {
                    setPercent(ContextCompat.getColor(context!!, R.color.progress_0))
                }
                missionPercent < 70 -> {
                    setPercent(ContextCompat.getColor(context!!, R.color.progress_40))
                }
                else -> {
                    setPercent(ContextCompat.getColor(context!!, R.color.progress_70))
                }
            }
        } else {
            binding.imgComplete.visibility = View.VISIBLE
            setPercent(ContextCompat.getColor(context!!, R.color.progress_100))
        }
    }

    fun setPercent(color: Int) {
        binding.textPercent.setTextColor(color)
        binding.progressPercent.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)

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