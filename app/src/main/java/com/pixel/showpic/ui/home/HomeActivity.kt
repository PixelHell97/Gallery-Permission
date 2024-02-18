package com.pixel.showpic.ui.home

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pixel.showpic.databinding.ActivityHomeBinding
import com.pixel.showpic.ui.Constants

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var imagePicker: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initGallery()
        openGallery()
    }

    private fun openGallery() {
        binding.contentHome.btnOpenGallery.setOnClickListener {
            requestGalleryPermission()
        }
    }

    private fun initGallery() {
        imagePicker =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { picUri ->
                binding.contentHome.photoContainer.setImageURI(picUri)
            }
    }

    private fun permissionNeeded(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            READ_MEDIA_IMAGES
        } else {
            READ_EXTERNAL_STORAGE
        }
    }

    private fun requestGalleryPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                permissionNeeded(),
            ) == PackageManager.PERMISSION_GRANTED -> {
                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permissionNeeded(),
            ) -> {
                showDialog(
                    "This app requires Storage permission for particular feature to work as expected.",
                    "Ok",
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permissionNeeded()),
                        Constants.PERMISSION_REQ_CODE,
                    )
                }
            }

            else -> {
                // requestGalleryPermissionLauncher.launch(permissionNeeded())
                requestPermissions(
                    arrayOf(permissionNeeded()),
                    Constants.PERMISSION_REQ_CODE,
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissionNeeded())) {
                    showDialog(
                        "Open gallery needs permission that you have denied." +
                            "Please allow Storage permission from settings to proceed further.",
                        "Settings",
                    ) { openSettings() }
                }
            }

            else -> {
                requestGalleryPermission()
            }
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

    private fun showDialog(
        message: String,
        posActionName: String? = null,
        posActionCallBack: (() -> Unit)? = null,
    ) {
        val permissionDialog = AlertDialog.Builder(this)
        permissionDialog.setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton(posActionName) { dialog, _ ->
                posActionCallBack?.invoke()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
