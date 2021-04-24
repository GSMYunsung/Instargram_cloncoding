package com.example.instargram_cloncoding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instargram_cloncoding.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

//이메일 로그인 : 이메일을 요청하게되면 파이어베이스에서 요청하는 두단계 구조
//소셜 로그인 : 구글로그인 -> 파이어베이스 요청 -> 파이어베이스가 구글에 로그인 요청

// open md~ -> play-services-auth 구글로그인 임포트

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    //Auth라이브러리 불러오기
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        //로그인 버튼이 눌렸을때 실행
       binding.emailLoginButton.setOnClickListener {
           //구글 로그인 순서 첫번
           //(1)
            singin_sunginup()
        }
        binding.googleSingInButton.setOnClickListener {
            google_log_in()
        }

        //구글 로그인 인증 토큰

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // 아이디 인증토
                .requestEmail() // 이메일을 받아올 수 있도록 해줌
                .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
    }

    fun google_log_in(){
        var signIntent = googleSignInClient?.signInIntent
        startActivityForResult(signIntent,GOOGLE_LOGIN_CODE) // 값리
    }

    //파이어베이스와 구글 로그인 연동
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)//구글에서 넘겨주는 결과값 저장
            if (result != null) { //null감
                if(result.isSuccess){
                    var account = result.signInAccount
                        //로그인 두번째 순번
                        //(2)
                        firebaseAuthWithGoogle(account)
                }

            }
        }
    }

    //파이어베이스 구글 인증
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //아이디와 페스워드가 일치하였을 때 실행해줄 코드
                        moveMainPage(task.result?.user)
                    } else {
                        //아이디와 패스워드가 일치하지 않았을때
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }

                }
    }

    //리스너를 설정해줌에따라 아이디와 비밀번호를 받아오고, 값을 받아오기 성공했을때 받아오지못했을때 등 각자의 상황을 실행 (회원가입)
    fun singin_sunginup(){
        auth?.createUserWithEmailAndPassword(binding.emailEdittext.text.toString(),binding.passwordEdittext.text.toString()) // 첫번째 이메일, 두번째 패스워드
                ?.addOnCompleteListener(this) {
                task ->
                    if(task.isSuccessful){
                        //아이디가 생성되었을 때 실행해줄 코드
                        moveMainPage(task.result?.user)
                    }
                    else if(!task.exception?.message.isNullOrEmpty()){
                        //에러 메세지 출력 코드
                        Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                    }
                    else{
                        //회원가입도 되지않고, 에러메세지도 출력이 불가능할경우 쓸 코드
                        signinEmail()
                    }
        }
    }

    //로그인을 할시에 실행할 실행 코드
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(binding.emailEdittext.text.toString(),binding.passwordEdittext.text.toString()) // 첫번째 이메일, 두번째 패스워드
                ?.addOnCompleteListener(this) {
                    task ->
                    if(task.isSuccessful){
                        //아이디와 페스워드가 일치하였을 때 실행해줄 코드
                        moveMainPage(task.result?.user)
                    }
                    else{
                        //아이디와 패스워드가 일치하지 않았을때
                        Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                    }
                }
    }

    //다음페이지로 넘어가는 코드
    fun moveMainPage(user:FirebaseUser?){
        //파이어베이스에 유저 정보를 넘겨주고 메인화면으로 넘겨는 코드
        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}