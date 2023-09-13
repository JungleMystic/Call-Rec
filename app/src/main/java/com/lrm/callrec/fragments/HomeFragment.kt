package com.lrm.callrec.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lrm.callrec.R
import com.lrm.callrec.constants.RECORD_AUDIO_PERMISSION_CODE
import com.lrm.callrec.constants.TAG
import com.lrm.callrec.constants.WRITE_EXTERNAL_STORAGE_PERMISSION_CODE
import com.lrm.callrec.databinding.FragmentHomeBinding
import com.lrm.callrec.utils.CallRecord
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var callRecord: CallRecord

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.blue)
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

        callRecord = CallRecord(requireContext())

        binding.startRec.setOnClickListener { startRecording() }
        binding.stopRec.setOnClickListener { stopRecording() }
        binding.playRec.setOnClickListener { callRecord.playRecording() }
        binding.stopPlay.setOnClickListener { callRecord.stopPlaying() }
    }

    private fun startRecording() {
        if (!hasRecordAudioPermission()) {
            requestRecordAudioPermission()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!hasWriteExternalStoragePermission()) {
                requestWriteExternalStoragePermission()
                return
            }
        }

        if (!isAccessibilityEnabled(requireContext())) {
            showTurnOnAccessibilityDialog()
            return
        }

        callRecord.startRecording()

        Toast.makeText(requireContext(), "Recording Audio...", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        callRecord.stopRecording()
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

    private fun hasWriteExternalStoragePermission(): Boolean =
        EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun requestWriteExternalStoragePermission() {
        EasyPermissions.requestPermissions(
            this,
            "Permission is required to record audio",
            WRITE_EXTERNAL_STORAGE_PERMISSION_CODE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
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
    }

    private fun showTurnOnAccessibilityDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Need to Turn on Accessibility Service")
            .setCancelable(true)
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .show()
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        var accessibilityEnabled = 0
        val service: String = context.packageName + "/com.lrm.callrec.services.CallRecordService"
        Log.i(TAG, "isAccessibilityEnabled: our service -> $service")
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
            Log.i(TAG, "isAccessibilityEnabled: $accessibilityEnabled")
        } catch (e: SettingNotFoundException) {
            Log.i(TAG, "Error finding setting, default accessibility to not found: " + e.message)
        }

        val colonSplit = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            Log.i(TAG, "isAccessibilityEnabled is enabled")
            val settingValue: String = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                colonSplit.setString(settingValue)
                while (colonSplit.hasNext()) {
                    val accessibilityService = colonSplit.next()
                    Log.i(TAG, "AccessibilityService :: \n $accessibilityService \n $service")
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        Log.i(TAG, "accessibility is switched on!")
                        return true
                    }
                }
            }
        } else {
            Log.i(TAG, "accessibility is disabled")
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}