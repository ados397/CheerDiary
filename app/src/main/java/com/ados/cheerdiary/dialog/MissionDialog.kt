package com.ados.cheerdiary.dialog


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.MissionDialogBinding
import com.ados.cheerdiary.model.DashboardMissionDTO
import com.ados.cheerdiary.model.ScheduleDTO


class MissionDialog(context: Context, private val isRange: Boolean = true) : Dialog(context), View.OnClickListener {

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

        missionCount = dashboardMissionDTO?.scheduleProgressDTO?.count!!
        missionCountMax = dashboardMissionDTO?.scheduleDTO?.count!!

        binding.textRate.text = "${missionCount}/${missionCountMax}"
        getPercent()

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
        binding.buttonMax.setOnClickListener {
            missionCount = missionCountMax
            binding.textRate.text = "${missionCount}/${missionCountMax}"

            getPercent()
        }

        binding.buttonExecuteApp.setOnClickListener {
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


    }

    private fun getPercent() {
        missionPercent = ((missionCount.toDouble() / missionCountMax) * 100).toInt()
        binding.progressPercent.progress = missionPercent

        binding.textPercent.text = "${missionPercent}%"
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