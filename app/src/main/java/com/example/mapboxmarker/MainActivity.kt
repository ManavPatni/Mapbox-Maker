package com.example.mapboxmarker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.mapboxmarker.databinding.ActivityMainBinding
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener


class MainActivity : AppCompatActivity(), PermissionsListener{

    // View binding
    private lateinit var binding: ActivityMainBinding

    // Permission manager
    private lateinit var permissionsManager: PermissionsManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Checking Required User Permissions
        permissionsManager = PermissionsManager(this)
        checkPermission()
        changeTheme(Style.STANDARD)

        binding.button.setOnClickListener {
            binding.mapView.visibility = View.INVISIBLE
            binding.radioGroup.visibility = View.VISIBLE
        }

        binding.button2.setOnClickListener { showAddMarkerDialog() }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_standard -> changeTheme(Style.STANDARD)
                R.id.rb_streets -> changeTheme(Style.MAPBOX_STREETS)
                R.id.rb_satellite -> changeTheme(Style.SATELLITE)
                R.id.rb_hybrid -> changeTheme(Style.SATELLITE_STREETS)
                R.id.rb_light -> changeTheme(Style.LIGHT)
                R.id.rb_dark -> changeTheme(Style.DARK)
                else -> changeTheme(Style.STANDARD)
            }
        }

    }

    private fun changeTheme(styleUrl: String) {
        binding.mapView.mapboxMap.loadStyle(styleUrl) { style ->
            //theme loaded
            binding.mapView.visibility = View.VISIBLE
            binding.radioGroup.visibility = View.GONE
        }

    }

    private fun checkPermission() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location
        } else {
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        val explanationMessage = "This app requires access to your location to function properly."
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            //enableLocationComponent()
        } else {
            Toast.makeText(this,"Could not center the map..",Toast.LENGTH_SHORT).show()
        }
    }

    private fun addAnnotationToMap(latitude: Double, longitude: Double) {
        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.ic_red_marker
        )?.let { bitmap ->
            val annotationApi = binding.mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(longitude, latitude))
                .withIconImage(bitmap)
            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    private fun showAddMarkerDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_marker, null)
        val latitudeEditText = dialogView.findViewById<EditText>(R.id.editTextLatitude)
        val longitudeEditText = dialogView.findViewById<EditText>(R.id.editTextLongitude)

        builder.setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val latitudeStr = latitudeEditText.text.toString()
                val longitudeStr = longitudeEditText.text.toString()
                if (latitudeStr.isNotEmpty() && longitudeStr.isNotEmpty()) {
                    val latitude = latitudeStr.toDouble()
                    val longitude = longitudeStr.toDouble()
                    addAnnotationToMap(latitude, longitude)
                } else {
                    Toast.makeText(this, "Please enter latitude and longitude", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setTitle("Add Marker")
            .show()
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int): Bitmap? =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

}
