package ir.mahakco.apps.koltinlocationwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    lateinit var locationWatch : LocationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        locationWatch = LocationUtils(this)
        locationWatch.requestUserLocation { locationResult ->
            val textView = findViewById<TextView>(R.id.textView)
            textView.text = "lat : ${locationResult.lastLocation.latitude} ,lng: ${locationResult.lastLocation.longitude}"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationWatch.onActivityResult(requestCode ,resultCode ,data)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationWatch.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationWatch.onPermissionResult(requestCode ,permissions ,grantResults)
    }
}