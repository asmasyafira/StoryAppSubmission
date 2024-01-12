package com.example.storyappsubmission.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.storyappsubmission.data.SsPreferences
import com.example.storyappsubmission.databinding.ActivitySplashScreenBinding
import com.example.storyappsubmission.viewmodel.UserViewModel
import com.example.storyappsubmission.viewmodel.ViewModelFactory

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ssPref = SsPreferences.getInstance(dataStore)
        val userViewModel = ViewModelProvider(this, ViewModelFactory(ssPref))[UserViewModel::class.java]

        userViewModel.getLogin().observe(this) {isLogin ->
            val intent = if (isLogin) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }

            binding.imgBangkit.animate()
                .setDuration(3000)
                .alpha(0f)
                .withEndAction {
                    startActivity(intent)
                    finish()
                }
        }
    }
}