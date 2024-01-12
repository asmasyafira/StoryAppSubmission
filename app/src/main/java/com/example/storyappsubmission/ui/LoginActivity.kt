package com.example.storyappsubmission.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.storyappsubmission.R
import com.example.storyappsubmission.data.DataLogin
import com.example.storyappsubmission.data.SsPreferences
import com.example.storyappsubmission.databinding.ActivityLoginBinding
import com.example.storyappsubmission.viewmodel.LoginViewModel
import com.example.storyappsubmission.viewmodel.UserViewModel
import com.example.storyappsubmission.viewmodel.ViewModelFactory

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animateObject()
        onClicked()

        val pref = SsPreferences.getInstance(dataStore)
        val userViewModel = ViewModelProvider(this, ViewModelFactory(pref))[UserViewModel::class.java]

        userViewModel.getLogin().observe(this) { loginTrue ->
            if (loginTrue) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        loginViewModel.message.observe(this) { msg ->
            loginResponse(loginViewModel.isError, msg, userViewModel)
        }

        loginViewModel.loading.observe(this) {
            onLoading(it)
        }
    }

    private fun onClicked() {
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.seePass.setOnCheckedChangeListener { _, checked ->
            binding.cvPass.transformationMethod = if (checked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            binding.cvPass.text?.let { binding.cvPass.setSelection(it.length) }
        }

        binding.btnLogin.setOnClickListener {
            binding.cvEmail.clearFocus()
            binding.cvPass.clearFocus()

            if (dataValid()) {
                val requestLogin = DataLogin(
                    binding.cvEmail.text.toString().trim(),
                    binding.cvPass.text.toString().trim()
                )
                loginViewModel.getLoginResponse(requestLogin)

            } else {
                if (!binding.cvEmail.emailValid) binding.cvEmail.error =
                    getString(R.string.noEmail)
                if (!binding.cvPass.passValid) binding.cvPass.error =
                    getString(R.string.noPass)

                Toast.makeText(this, "Login Salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dataValid(): Boolean {
        return binding.cvEmail.emailValid && binding.cvPass.passValid
    }

    private fun animateObject() {
        ObjectAnimator.ofFloat(binding.imgBangkit, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val cvEmail = ObjectAnimator.ofFloat(binding.cvEmail, View.ALPHA, 1f).setDuration(300)
        val cvPass = ObjectAnimator.ofFloat(binding.cvPass, View.ALPHA, 1f).setDuration(300)
        val seePass = ObjectAnimator.ofFloat(binding.seePass, View.ALPHA, 1f).setDuration(300)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(300)
        val btnRegis = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(300)
        val txtNotRegis = ObjectAnimator.ofFloat(binding.txtNotRegis, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(cvEmail, cvPass, seePass, btnLogin, btnRegis, txtNotRegis)
            start()
        }
    }

    private fun onLoading(it: Boolean) {
        binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
    }

    private fun loginResponse(error: Boolean, msg: String, userViewModel: UserViewModel) {
        if (!error) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            val user = loginViewModel.userLogin.value
            userViewModel.saveLogin(true)
            user?.loginResult!!.token.let { userViewModel.saveToken(it) }
            user.loginResult.name.let { userViewModel.saveName(it) }
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}