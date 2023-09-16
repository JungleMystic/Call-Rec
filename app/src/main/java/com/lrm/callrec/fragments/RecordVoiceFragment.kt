package com.lrm.callrec.fragments

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lrm.callrec.R
import com.lrm.callrec.constants.READ_AUDIO_PERMISSION_CODE
import com.lrm.callrec.constants.RECORD_AUDIO_PERMISSION_CODE
import com.lrm.callrec.constants.TAG
import com.lrm.callrec.constants.WRITE_EXTERNAL_STORAGE_PERMISSION_CODE
import com.lrm.callrec.databinding.FragmentRecordVoiceBinding
import com.lrm.callrec.utils.Timer
import com.lrm.callrec.utils.VoiceRecorder
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class RecordVoiceFragment : Fragment(), EasyPermissions.PermissionCallbacks, Timer.OnTimeTickListener {
    private var _binding: FragmentRecordVoiceBinding? = null
    private val binding get() = _binding!!

    private lateinit var voiceRec: VoiceRecorder
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordVoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voiceRec = VoiceRecorder(requireContext())
        timer = Timer(this)
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        binding.startRec.setOnClickListener { startRecording() }
        binding.pauseRec.setOnClickListener { pauseRecording() }
        binding.resumeRec.setOnClickListener { resumeRecording() }
        binding.stopRec.setOnClickListener { stopRecording() }
    }

    private fun startRecording() {
        if (!checkPermissions()) {
            showPermissionsRequiredDialog()
            return
        }

        voiceRec.startRecording()
        timer.startTimer()
        smallVibration()

        binding.timer.visibility = View.VISIBLE
        binding.startRec.visibility = View.GONE
        binding.pauseStopLl.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Recording Audio...", Toast.LENGTH_SHORT).show()
    }

    private fun pauseRecording() {
        voiceRec.pauseRecording()
        timer.pauseTimer()
        smallVibration()

        binding.recordingStatus.visibility = View.VISIBLE
        binding.resumeRec.visibility = View.VISIBLE
        binding.pauseRec.visibility = View.GONE
        binding.recordingStatus.text = voiceRec.recordingStatus
    }

    private fun resumeRecording() {
        voiceRec.resumeRecording()
        timer.startTimer()
        smallVibration()

        binding.recordingStatus.visibility = View.INVISIBLE
        binding.resumeRec.visibility = View.GONE
        binding.pauseRec.visibility = View.VISIBLE
    }

    private fun stopRecording() {
        voiceRec.stopRecording()
        timer.stopTimer()
        smallVibration()

        binding.timer.text = resources.getString(R.string.chronometer_text)
        binding.timer.visibility = View.INVISIBLE
        binding.pauseStopLl.visibility = View.INVISIBLE
        binding.startRec.visibility = View.VISIBLE
        binding.recordingStatus.visibility = View.INVISIBLE

        Toast.makeText(requireContext(), "Recording stopped...", Toast.LENGTH_SHORT).show()
    }

    override fun onTimerTick(duration: String) {
        binding.timer.text = duration
        Log.i(TAG, "onTimerTick -> duration -> $duration ")
    }

    private fun smallVibration() {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun hasRecordAudioPermission(): Boolean =
        EasyPermissions.hasPermissions(requireContext(), Manifest.permission.RECORD_AUDIO)

    private fun requestRecordAudioPermission() {
        EasyPermissions.requestPermissions(
            this,
            "Permission is required to record audio",
            RECORD_AUDIO_PERMISSION_CODE,
            Manifest.permission.RECORD_AUDIO
        )
    }

    private fun hasReadAudioPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun requestReadAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            EasyPermissions.requestPermissions(
                this,
                "Permission is required to record audio",
                READ_AUDIO_PERMISSION_CODE,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Permission is required to record audio",
                READ_AUDIO_PERMISSION_CODE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun hasWriteExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            voiceRec.createDirectory()
            true
        }
    }

    private fun requestWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "Permission is required to record audio",
                WRITE_EXTERNAL_STORAGE_PERMISSION_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireContext()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
        if (!checkPermissions()) requestPermissionsRequired()
    }

    private fun showPermissionsRequiredDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permissions Request")
            .setMessage("This app requires some permissions, So please allow them.")
            .setCancelable(true)
            .setPositiveButton("Ok") { _, _ ->
                requestPermissionsRequired()
            }
            .show()
    }

    private fun checkPermissions(): Boolean {
        if (!hasRecordAudioPermission()) {
            return false
        }

        if (!hasWriteExternalStoragePermission()) {
            return false
        }

        if (!hasReadAudioPermission()) {
            return false
        }

        return true
    }

    private fun requestPermissionsRequired() {
        if (!hasRecordAudioPermission()) {
            requestRecordAudioPermission()
            return
        }

        if (!hasWriteExternalStoragePermission()) {
            requestWriteExternalStoragePermission()
            return
        }

        if (!hasReadAudioPermission()) {
            requestReadAudioPermission()
            return
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}