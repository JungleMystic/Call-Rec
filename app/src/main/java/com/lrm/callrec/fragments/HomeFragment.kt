package com.lrm.callrec.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lrm.callrec.R
import com.lrm.callrec.constants.READ_AUDIO_PERMISSION_CODE
import com.lrm.callrec.constants.RECORD_AUDIO_PERMISSION_CODE
import com.lrm.callrec.constants.WRITE_EXTERNAL_STORAGE_PERMISSION_CODE
import com.lrm.callrec.databinding.FragmentHomeBinding
import com.lrm.callrec.utils.VoiceRecorder
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var voiceRec: VoiceRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.blue)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voiceRec = VoiceRecorder(requireContext())

        if (!checkPermissions()) showPermissionsRequiredDialog()

        binding.startRec.setOnClickListener { startRecording() }
        binding.pauseRec.setOnClickListener { pauseRecording() }
        binding.resumeRec.setOnClickListener { resumeRecording() }
        binding.stopRec.setOnClickListener { stopRecording() }
    }

    private fun startRecording() {
        if (!checkPermissions()){
            requestPermissionsRequired()
            return
        }
        voiceRec.startRecording()

        binding.startRec.visibility = View.GONE
        binding.pauseStopLl.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Recording Audio...", Toast.LENGTH_SHORT).show()
    }

    private fun pauseRecording() {
        voiceRec.pauseRecording()
        binding.recordingStatus.visibility = View.VISIBLE
        binding.resumeRec.visibility = View.VISIBLE
        binding.pauseRec.visibility = View.GONE
        binding.recordingStatus.text = voiceRec.recordingStatus
    }

    private fun resumeRecording() {
        voiceRec.resumeRecording()
        binding.recordingStatus.visibility = View.GONE
        binding.resumeRec.visibility = View.GONE
        binding.pauseRec.visibility = View.VISIBLE
    }

    private fun stopRecording() {
        voiceRec.stopRecording()
        binding.pauseStopLl.visibility = View.GONE
        binding.startRec.visibility = View.VISIBLE
        binding.recordingStatus.visibility = View.GONE
        Toast.makeText(requireContext(), "Recording stopped...", Toast.LENGTH_SHORT).show()
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
            EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
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