package com.lrm.voicerec.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lrm.voicerec.R
import com.lrm.voicerec.constants.TAG
import com.lrm.voicerec.database.AudioFile
import com.lrm.voicerec.databinding.RecListItemBinding
import com.lrm.voicerec.utils.VoicePlayer
import com.lrm.voicerec.viewmodel.RecViewModel
import java.io.File

class RecListAdapter(
    private val activity: Activity,
    private val context: Context,
    private val recViewModel: RecViewModel,
    private val voicePlayer: VoicePlayer
): ListAdapter<AudioFile, RecListAdapter.RecViewHolder>(DiffCallback) {

    inner class RecViewHolder(private val binding: RecListItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bindData(audioFile: AudioFile) {
            binding.fileName.text = audioFile.fileName

            binding.playBtn.setOnClickListener {
                voicePlayer.stopPlaying()
                voicePlayer.startPlaying(audioFile.filePath)
                binding.playBtn.visibility = View.INVISIBLE
                binding.stopBtn.visibility = View.VISIBLE
            }
            binding.stopBtn.setOnClickListener {
                voicePlayer.stopPlaying()
                binding.playBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
            }
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
        holder.itemView.setOnLongClickListener {
            showDeleteDialog(audioFile)
            true
        }
    }

    private fun showDeleteDialog(audioFile: AudioFile) {
        val dialogView = activity.layoutInflater.inflate(R.layout.custom_delete_dialog, null)
        val yesTv = dialogView.findViewById<TextView>(R.id.yes_tv)
        val noTv = dialogView.findViewById<TextView>(R.id.no_tv)

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        builder.setCancelable(true)

        val deleteDialog = builder.create()
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteDialog.show()

        yesTv.setOnClickListener {
            recViewModel.deleteFile(audioFile)
            try {
                File(audioFile.filePath).delete()
            } catch (e: Exception) {
                Log.i(TAG, "showDeleteDialog: Exception occurred while deleting file -> ${e.message}")
            }
            deleteDialog.dismiss()
        }

        noTv.setOnClickListener {
            deleteDialog.dismiss()
        }
    }
}