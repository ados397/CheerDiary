package com.ados.cheerdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.ados.cheerdiary.databinding.ActivityJoinBinding
import com.ados.cheerdiary.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*


class JoinActivity : AppCompatActivity() {
    private var _binding: ActivityJoinBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    private var emailOK: Boolean = false
    private var passwordOK: Boolean = false
    private var passwordConfirmOK: Boolean = false
    private var nicknameOK: Boolean = false

    private var userDTO: UserDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        userDTO = intent.getParcelableExtra("user")
        if (userDTO != null) { // null 이 아니라면 소셜 로그인, 이메일, 비밀번호는 입력하지 않음
            binding.editEmail.setText(userDTO?.userId)
            binding.editEmail.isEnabled = false
            emailOK = true

            binding.editPassword.setText("password_sample")
            binding.editPassword.isEnabled = false
            passwordOK = true

            binding.editPasswordConfirm.setText("password_sample")
            binding.editPasswordConfirm.isEnabled = false
            passwordConfirmOK = true
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonOk.setOnClickListener {
            var email = binding.editEmail.text.toString().trim()
            var nickname = binding.editNickname.text.toString().trim()
            var password = binding.editPassword.text.toString().trim()

            firestore?.collection("user")?.get()?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    for (document in task.result) {
                        var user = document.toObject(UserDTO::class.java)!!
                        when {
                            user.userId!! == email -> {
                                Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }
                            user.nickname!! == nickname -> {
                                Toast.makeText(this, "닉네임이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }
                        }
                    }

                    if (userDTO != null) { // null 이 아니라면 소셜 로그인, 이미 로그인 처리는 되어 있음, firestore에 데이터 기록 후 메인페이지 이동
                        writeFirestoreAndFinish(UserDTO(firebaseAuth?.currentUser?.uid, email, userDTO?.loginType, nickname, 1, 0.0, null, null, "", "", Date()))
                    } else {
                        firebaseAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                writeFirestoreAndFinish(UserDTO(firebaseAuth?.currentUser?.uid, email, UserDTO.LoginType.EMAIL, nickname, 1, 0.0, null, null, "", "", Date()))
                            } else if (!task.exception?.message.isNullOrEmpty()) {
                                Toast.makeText(this, "회원가입에 실패하였습니다. 잠시 후 다시 시도해 보세요.", Toast.LENGTH_SHORT).show()
                                //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            /*firestore?.collection("user")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                for (snapshot in querySnapshot!!.documents) {
                    println("기존:${snapshot.getString("userId")!!}, 신규:${email}")
                    when {
                        snapshot.getString("userId")!!.contains(email) -> {
                            Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                            return@addSnapshotListener
                        }
                        snapshot.getString("nickName")!!.contains(nickname) -> {
                            Toast.makeText(this, "닉네임이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                            return@addSnapshotListener
                        }
                    }
                }

                firebaseAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        writeFirestore(UserDTO(firebaseAuth?.currentUser?.uid, email, nickname, null))
                        Toast.makeText(this, "회원가입이 완료 되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (!task.exception?.message.isNullOrEmpty()) {
                        Toast.makeText(this, "회원가입에 실패하였습니다. 잠시 후 다시 시도해 보세요.", Toast.LENGTH_SHORT).show()
                        //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }

            }*/

            /*firestore?.collection("user")?.whereEqualTo("userId", binding.editEmail.text)?.get()?.addOnCompleteListener { task ->
                if(task.isSuccessful){

                    Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            firestore?.collection("user")?.whereEqualTo("nickName", binding.editNickname.text)?.get()?.addOnCompleteListener { task ->
                if(task.isSuccessful){

                    Toast.makeText(this, "닉네임이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                }
            }*/


            // 이미 존재하는 닉네임 입니다.


        }

        binding.editEmail.doAfterTextChanged {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.editEmail.text).matches()) {
                binding.textEmailError.text = "이메일 형식으로 입력해 주세요."
                binding.editEmail.setBackgroundResource(R.drawable.edit_rectangle_red)
                emailOK = false
            } else {
                binding.textEmailError.text = ""
                binding.editEmail.setBackgroundResource(R.drawable.edit_rectangle)
                emailOK = true
            }

            if (binding.editEmail.text.toString().isEmpty())
                emailOK = false

            visibleOkButton()
        }

        binding.editPassword.doAfterTextChanged {
            if (!isValidPassword(binding.editPassword.text.toString())) {
                binding.textPasswordError.text = "비밀번호는 6자 이상 숫자, 영문, 특수문자 중 2가지가 포함되어야 합니다."
                binding.editPassword.setBackgroundResource(R.drawable.edit_rectangle_red)
                passwordOK = false
            } else {
                binding.textPasswordError.text = ""
                binding.editPassword.setBackgroundResource(R.drawable.edit_rectangle)
                passwordOK = true
            }

            if (binding.editPassword.text.toString().isEmpty())
                passwordOK = false

            isValidPasswordConfirm()

            visibleOkButton()
        }

        binding.editPasswordConfirm.doAfterTextChanged {
            isValidPasswordConfirm()

            if (binding.editPasswordConfirm.text.toString().isEmpty())
                passwordConfirmOK = false

            visibleOkButton()
        }

        binding.editNickname.doAfterTextChanged {
            if (!isValidNickname(binding.editNickname.text.toString())) {
                binding.textNicknameError.text = "사용할 수 없는 문자열이 포함되어 있습니다."
                binding.editNickname.setBackgroundResource(R.drawable.edit_rectangle_red)
                nicknameOK = false
            } else {
                binding.textNicknameError.text = ""
                binding.editNickname.setBackgroundResource(R.drawable.edit_rectangle)
                nicknameOK = true
            }

            if (binding.editNickname.text.toString().isEmpty())
                nicknameOK = false

            binding.textNicknameLen.text = "${binding.editNickname.text.length}/15"

            visibleOkButton()
        }
    }

    private fun writeFirestoreAndFinish(user: UserDTO) {
        firestore?.collection("user")?.document(user.uid.toString())?.set(user)?.addOnCompleteListener {
            Toast.makeText(this, "회원가입이 완료 되었습니다.", Toast.LENGTH_SHORT).show()
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)

            finish()
        }
    }

    private fun isValidPasswordConfirm() {
        if (binding.editPasswordConfirm.text.toString() != binding.editPassword.text.toString()) {
            binding.textPasswordConfirmError.text = "비밀번호가 일치하지 않습니다."
            binding.editPasswordConfirm.setBackgroundResource(R.drawable.edit_rectangle_red)
            passwordConfirmOK = false
        } else {
            binding.textPasswordConfirmError.text = ""
            binding.editPasswordConfirm.setBackgroundResource(R.drawable.edit_rectangle)
            passwordConfirmOK = true
        }
    }

    private fun isKorean(s: String): Boolean {
        var i = 0
        while (i < s.length) {
            val c = s.codePointAt(i)
            if (c in 0xAC00..0xD800)
                return true
            i += Character.charCount(c)
        }
        return false
    }


    private fun isValidPassword(password: String) : Boolean {
        if (password.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*\$".toRegex())) {
            return false
        }

        return password.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9~!@#\$%^&*]).{6,30}\$".toRegex())
    }

    private fun isValidNickname(nickname: String) : Boolean {
        val exp = Regex("^[가-힣ㄱ-ㅎa-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_-]{1,15}\$")
        return !nickname.isNullOrEmpty() && exp.matches(nickname)
    }

    private fun visibleOkButton() {
        binding.buttonOk.isEnabled = emailOK && passwordOK && passwordConfirmOK && nicknameOK
    }
}