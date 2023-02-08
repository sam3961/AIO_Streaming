package com.google.location.nearby.apps.walkietalkies

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.location.nearby.apps.walkietalkies.utils.PermissionUtils


open class SplashActivity : AppCompatActivity() {

    private val REQUEST_LOCATION_CODE = 1
    private val REQUEST_BLUETOOTH_CODE = 2
    private val BACKGROUND_REQUEST_CODE = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val backgroundImage: ImageView = findViewById(R.id.imageViewSplash)

        if (BuildConfig.ROLE == Constants.BROADCASTER)
            backgroundImage.setImageResource(R.drawable.ic_broadcaster)
        else
            backgroundImage.setImageResource(R.drawable.ic_receiver)


        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.side_slide)
        backgroundImage.startAnimation(slideAnimation)
        Handler(Looper.getMainLooper()).postDelayed({
            //locationCheck()
           // navigateToHome()
            bluetoothPermissions()
        }, 3000)
    }


    open fun openBluetoothSettings() {
        Toast.makeText(applicationContext, "Please turn on bluetooth", Toast.LENGTH_SHORT).show()
        val intentOpenBluetoothSettings = Intent()
        intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
        startActivityForResult(intentOpenBluetoothSettings, REQUEST_BLUETOOTH_CODE)
    }

    open fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    REQUEST_LOCATION_CODE
                )
            }
            .setNegativeButton(
                "No"
            ) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }


    open fun isBlueToothOn(): Boolean {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        return btAdapter != null && btAdapter.isEnabled
    }

    open fun locationCheck() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        } else {
            if (!isBlueToothOn())
                openBluetoothSettings()
            else {
                // navigateToHome()
                bluetoothPermissions()
            }

        }
    }

    private fun bluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }
/*        else {
            backgroundLocationPermission()
        }*/
        else {
            navigateToHome()
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value) {
                    backgroundLocationPermission()
                    return@forEach
                }
            }

        }

    private fun backgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && BuildConfig.ROLE == Constants.BROADCASTER
            && !PermissionUtils.hasPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_REQUEST_CODE
            )
        } else
            navigateToHome()


    }

    private fun navigateToHome() {
        if (BuildConfig.ROLE == Constants.BROADCASTER)
            startActivity(Intent(this, BroadcasterActivity::class.java))
        else
            startActivity(Intent(this@SplashActivity, ReceiverActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LOCATION_CODE) {
                locationCheck()
            } else if (requestCode == REQUEST_BLUETOOTH_CODE) {
                if (!isBlueToothOn())
                    openBluetoothSettings()
                else
                    bluetoothPermissions()

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BACKGROUND_REQUEST_CODE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    navigateToHome()

                } else {
                    navigateToHome()
                    //   backgroundLocationPermission()
                }
                // navigateToHome()
                return
            }
        }

    }
}