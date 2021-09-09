package com.ados.cheerdiary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.ados.cheerdiary.databinding.ActivityMainBinding
import com.ados.cheerdiary.dialog.QuestionDialog
import com.ados.cheerdiary.model.QuestionDTO
import com.ados.cheerdiary.model.UserDTO
import com.ados.cheerdiary.page.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.question_dialog.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tabIcon = listOf(
        R.drawable.schedule,
        R.drawable.fan_club,
        R.drawable.schedule,
        R.drawable.schedule,
    )

    private val tabLayoutText = arrayOf("상황판", "스케줄", "계정설정", "더보기")

    private var currentUser: UserDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("user")
        if (currentUser == null) {
            val question = QuestionDTO(
                QuestionDTO.STAT.ERROR,
                "사용자 확인 실패",
                "로그인 정보가 올바르지 않습니다.\n앱을 종료합니다."
            )
            val dialog = QuestionDialog(this, question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.showButtonOk(false)
            dialog.setButtonCancel("확인")
            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
                appExit()
            }
        }

        binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
        binding.viewpager.apply {
            adapter = MyPagerAdapter(context as FragmentActivity)
            setPageTransformer(ZoomOutPageTransformer())
        }

        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
            //tab.text = "${tabLayoutText[position]}"
            tab.setIcon(tabIcon[position])
        }.attach()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        appExit()
    }

    fun getUser() : UserDTO {
        return currentUser!!
    }

    private fun appExit() {
        finishAffinity() //해당 앱의 루트 액티비티를 종료시킨다. (API  16미만은 ActivityCompat.finishAffinity())
        System.runFinalization() //현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.
        exitProcess(0) // 현재 액티비티를 종료시킨다.
    }
}