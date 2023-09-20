package com.lrm.voicerec.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lrm.voicerec.R
import com.lrm.voicerec.VoiceRecApplication
import com.lrm.voicerec.adapter.RecListAdapter
import com.lrm.voicerec.constants.READ_AUDIO_PERMISSION_CODE
import com.lrm.voicerec.constants.RECORD_AUDIO_PERMISSION_CODE
import com.lrm.voicerec.constants.WRITE_EXTERNAL_STORAGE_PERMISSION_CODE
import com.lrm.voicerec.databinding.FragmentHomeBinding
import com.lrm.voicerec.utils.Timer
import com.lrm.voicerec.utils.VoiceRecorder
import com.lrm.voicerec.viewmodel.RecViewModel
import com.lrm.voicerec.viewmodel.RecViewModelFactory
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks, Timer.OnTimeTickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val recViewModel: RecViewModel by activityViewModels() {
        RecViewModelFactory(
            (activity?.application as VoiceRecApplication).database.audioFileDao()
        )
    }

    private lateinit var voiceRec: VoiceRecorder
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator

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
        timer = Timer(this)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!checkPermissions()) showPermissionsRequiredDialog()

        val adapter = RecListAdapter(requireContext())
        binding.recRv.adapter = adapter

        recViewModel.getAll.observe(this.viewLifecycleOwner) {list ->
            if (list.isEmpty()) {
                binding.noRecordings.visibility = View.VISIBLE
                binding.recRv.visibility = View.INVISIBLE
            } else {
                binding.noRecordings.visibility = View.INVISIBLE
                binding.recRv.visibility = View.VISIBLE
                list.let { adapter.submitList(it) }
            }
        }

        binding.startRec.setOnClickListener { startRecording() }
        binding.pauseRec.setOnClickListener { pauseRecording() }
        binding.resumeRec.setOnClickListener { resumeRecording() }
        binding.stopRec.setOnClickListener { stopRecording() }

        binding.appName.setOnLongClickListener {
            showDeveloperInfoDialog()
            true
        }
    }

    private fun startRecording() {
        if (!checkPermissions()) {
            showPermissionsRequiredDialog()
            return
        }
        voiceRec.startRecording()
        timer.startTimer()
        vibrateOnTap()

        binding.timer.visibility = View.VISIBLE
        binding.recRv.visibility = View.INVISIBLE
        binding.startRec.visibility = View.GONE
        binding.pauseStopLl.visibility = View.VISIBLE
        //Toast.makeText(requireContext(), "Recording Audio...", Toast.LENGTH_SHORT).show()
    }

    private fun pauseRecording() {
        voiceRec.pauseRecording()
        timer.pauseTimer()
        vibrateOnTap()

        binding.recordingStatus.visibility = View.VISIBLE
        binding.resumeRec.visibility = View.VISIBLE
        binding.pauseRec.visibility = View.GONE
        binding.recordingStatus.text = voiceRec.recordingStatus
    }

    private fun resumeRecording() {
        voiceRec.resumeRecording()
        timer.startTimer()
        vibrateOnTap()

        binding.recordingStatus.visibility = View.INVISIBLE
        binding.resumeRec.visibility = View.GONE
        binding.pauseRec.visibility = View.VISIBLE
    }

    private fun stopRecording() {
        voiceRec.stopRecording()
        timer.stopTimer()
        vibrateOnTap()

        binding.timer.text = resources.getString(R.string.chronometer_text)
        binding.timer.visibility = View.INVISIBLE
        binding.recRv.visibility = View.VISIBLE
        binding.pauseStopLl.visibility = View.INVISIBLE
        binding.startRec.visibility = View.VISIBLE
        binding.recordingStatus.visibility = View.INVISIBLE
        Toast.makeText(requireContext(), "Recording stopped...", Toast.LENGTH_SHORT).show()
    }

    override fun onTimerTick(duration: String) {
        binding.timer.text = duration
    }

    private fun vibrateOnTap() {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun showDeveloperInfoDialog() {
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.custom_developer_info, null)
        val imageLink = "https://firebasestorage.googleapis.com/v0/b/gdg-vizag-f9bf0.appspot.com/o/gdg_vizag%2Fdeveloper%2FRammohan_L_pic.png?alt=media&token=6e55ba28-e0ca-45c6-b50b-be1955da2566"
        val devImage = dialogView.findViewById<CircleImageView>(R.id.dev_image)
        Glide.with(requireContext()).load(imageLink).placeholder(R.drawable.loading_icon_anim).into(devImage)

        val devGithubLink = dialogView.findViewById<CircleImageView>(R.id.dev_github_link)
        val devYoutubeLink = dialogView.findViewById<CircleImageView>(R.id.dev_youtube_link)

        devGithubLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JungleMystic"))
            startActivity(intent)
        }

        devYoutubeLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@junglemystic"))
            startActivity(intent)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        builder.setCancelable(true)

        val developerDialog = builder.create()
        developerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        developerDialog.show()
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

    override fun onStop() {
        super.onStop()
        voiceRec.stopRecording()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        voiceRec.stopRecording()
        _binding = null
    }
}