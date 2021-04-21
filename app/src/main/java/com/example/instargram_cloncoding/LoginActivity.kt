package com.example.instargram_cloncoding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instargram_cloncoding.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    var auth : FirebaseAuth? = null

    //Auth라이브러리 불러오기
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        //로그인 버튼이 눌렸을때 실행
       binding.emailLoginButton.setOnClickListener {
            signinEmail()
        }
    }

    //리스너를 설정해줌에따라 아이디와 비밀번호를 받아오고, 값을 받아오기 성공했을때 받아오지못했을때 등 각자의 상황을 실행 (회원가입)
    fun singin_sunginup(){
        auth?.createUserWithEmailAndPassword(binding.emailEdittext.toString(),binding.passwordEdittext.toString()) // 첫번째 이메일, 두번째 패스워드
                ?.addOnCompleteListener {
                task ->
                    if(task.isSuccessful){
                        //아이디가 생성되었을 때 실행해줄 코드
                        moveMainPage(task.result?.user)
                    }
                    else if(task.exception?.message.isNullOrEmpty()){
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
        auth?.signInWithEmailAndPassword(binding.emailEdittext.toString(),binding.passwordEdittext.toString()) // 첫번째 이메일, 두번째 패스워드
                ?.addOnCompleteListener {
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