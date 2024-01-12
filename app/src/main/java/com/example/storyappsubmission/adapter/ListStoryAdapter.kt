package com.example.storyappsubmission.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyappsubmission.R
import com.example.storyappsubmission.data.StoryDetailResponse
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListStoryAdapter(private val storyList: List<StoryDetailResponse>) :
    RecyclerView.Adapter<ListStoryAdapter.ListViewHolder>() {

    companion object {
        @JvmStatic
        fun dateToString(stringDate: String): String {
            val formatInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val formatOutput = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val date: Date?
            var dateOutput = ""
            try {
                date = formatInput.parse(stringDate)
                dateOutput = formatOutput.format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return dateOutput
        }
    }

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: StoryDetailResponse)
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imgStory : ImageView = itemView.findViewById(R.id.imgStory)
        val titleStory: TextView = itemView.findViewById(R.id.titleStory)
        val descStory: TextView = itemView.findViewById(R.id.descStory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_row_story, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listStory = storyList[position]
        Glide.with(holder.itemView.context)
            .load(listStory.photoUrl)
            .into(holder.imgStory)
        holder.titleStory.text = listStory.name
        holder.descStory.text = listStory.description
        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(storyList[holder.adapterPosition])
        }
    }

    override fun getItemCount(): Int = storyList.size
}