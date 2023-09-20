package com.lrm.voicerec.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lrm.voicerec.database.AudioFile
import com.lrm.voicerec.databinding.RecListItemBinding

class RecListAdapter(
    private val context: Context,
): ListAdapter<AudioFile, RecListAdapter.RecViewHolder>(DiffCallback) {

    inner class RecViewHolder(private val binding: RecListItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bindData(audioFile: AudioFile) {
            binding.title.text = audioFile.fileName
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AudioFile>() {
        override fun areItemsTheSame(oldItem: AudioFile, newItem: AudioFile): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AudioFile, newItem: AudioFile): Boolean {
            return oldItem.fileName == newItem.fileName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        return RecViewHolder(
            RecListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {
        val audioFile = getItem(position)
        holder.bindData(audioFile)
    }
}