package com.example.sns_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.sns_app.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인 버튼을 눌렀을 때
        binding.btnLogin.setOnClickListener {
            val userEmail = binding.email.text.toString()
            val userPassword = binding.password.text.toString()
            doLogin(userEmail, userPassword)
        }

        // 회원가입 버튼을 눌렀을 때
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

    }

    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if(it.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java)) // 메인메뉴 Activity 연결해주기!!!!!
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}