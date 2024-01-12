package com.example.storyappsubmission.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.storyappsubmission.adapter.ListStoryAdapter
import com.example.storyappsubmission.data.StoryDetailResponse
import com.example.storyappsubmission.databinding.ActivityDetailBinding

@Suppress("DEPRECATION")
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    companion object {
        const val EXTRA_DATA = "EXTRA_DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val detailStory = intent.getParcelableExtra<StoryDetailResponse>(EXTRA_DATA) as StoryDetailResponse
        setStory(detailStory)
    }

    private fun setStory(detailStory: StoryDetailResponse) {
        Glide.with(this)
            .load(detailStory.photoUrl)
            .into(binding.imgDetail)

        binding.apply {
            nameDetail.text = detailStory.name
            descDetail.text = detailStory.description
            dateDetail.text = ListStoryAdapter.dateToString(detailStory.createdAt)
        }
    }
}