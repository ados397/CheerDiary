package com.ados.cheerdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.ados.cheerdiary.databinding.ActivityLoginBinding
import com.ados.cheerdiary.model.UserDTO
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null
    private var googleSignInClient : GoogleSignInClient? = null

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var callbackManager : CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 구글 로그인 처리
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            println("리절트 ${result.resultCode}")
            if (result.resultCode == RESULT_OK) {
                println("오케이~")
                var result = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data)
                if (result.isSuccess) {
                    var account = result.signInAccount
                    firebaseAuthWithGoogle(account)
                }
            }
        }

        // 구글 로그인 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        callbackManager = CallbackManager.Factory.create()

        binding.buttonJoin.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }

        binding.editPassword.setOnKeyListener { view, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KEYCODE_ENTER) {
                login()
            }
            false
        }

        binding.buttonLogin.setOnClickListener {
            login()
        }

        binding.buttonLoginGoogle.setOnClickListener {
            val signInIntent = googleSignInClient?.signInIntent
            //startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
            resultLauncher.launch(signInIntent)
        }

        binding.buttonLoginFacebook.setOnClickListener {
            facebookLogin()
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result?.accessToken)

            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    private fun login() {
        val email = binding.editEmail.text.toString().trim()
        when {
            email.isNullOrEmpty() -> {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            binding.editPassword.text.isNullOrEmpty() -> {
                Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                firebaseAuth?.signInWithEmailAndPassword(email, binding.editPassword.text.toString())?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firestore?.collection("user")?.document(firebaseAuth?.uid!!)?.get()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("조회 성공? $task")
                                if (task.result.exists()) { // document 있음
                                    var user = task.result.toObject(UserDTO::class.java)!!
                                    callMainActivity(user)
                                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                                } else { // document 없으면 회원 가입 페이지로 이동
                                    Toast.makeText(this, "로그인에 실패하였습니다. 관리자에게 문의 하세요.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else if (!task.exception?.message.isNullOrEmpty()) {
                        //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        // 구글 로그인은 동일한 이메일로 로그인한 계정(페이스북, 이메일)이 있어도 로그인이 되어버리기 때문에 이미 가입된 정보가 있는지 확인해서 없을때만 처리
        firestore?.collection("user")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    var user = document.toObject(UserDTO::class.java)!!
                    if (user.loginType != UserDTO.LoginType.GOOGLE) {
                        when {
                            user.userId!! == account?.email.toString() -> {
                                Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                                googleSignInClient?.signOut()?.addOnCompleteListener {

                                }
                                return@addOnCompleteListener
                            }
                        }
                    }
                }

                var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
                firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "구글 로그인 성공", Toast.LENGTH_SHORT).show()
                        loginOrJoin(UserDTO(firebaseAuth?.currentUser?.uid, firebaseAuth?.currentUser?.email, UserDTO.LoginType.GOOGLE, null, 1, 0.0, null, null, "", "", Date()))
                    } else if (!task.exception?.message.isNullOrEmpty()) {
                        //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        //Toast.makeText(this, "구글 로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "페이스북 로그인 성공", Toast.LENGTH_SHORT).show()
                loginOrJoin(UserDTO(firebaseAuth?.currentUser?.uid, firebaseAuth?.currentUser?.email, UserDTO.LoginType.FACEBOOK, null, 1, 0.0, null, null, "", "", Date()))
            } else if (!task.exception?.message.isNullOrEmpty()) {
                //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "페이스북 로그인에 실패하였습니다. 동일한 이메일을 사용하는 사용자가 이미 존재할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginOrJoin(userDTO: UserDTO) {
        println("유저정보 ${userDTO}")
        firestore?.collection("user")?.document(userDTO.uid!!)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("조회 성공? $task")
                if (task.result.exists()) { // document 있음
                    var user = task.result.toObject(UserDTO::class.java)!!
                    if (user.nickname.isNullOrEmpty()) { // 닉네임이 없으면 회원 가입 페이지로 이동
                        callJoinActivity(user)
                    } else {
                        callMainActivity(user)
                    }
                } else { // document 없으면 회원 가입 페이지로 이동
                    println("조회 실패")
                    callJoinActivity(userDTO)
                }
            }
        }
    }

    private fun callJoinActivity(user: UserDTO) {
        var intent = Intent(this, JoinActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun callMainActivity(user: UserDTO) {
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
        finish()
    }
}