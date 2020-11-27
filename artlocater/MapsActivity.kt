package us.ait.artlocater

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toIcon
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_maps.*
import us.ait.artlocater.Data.Post
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*
import kotlin.random.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, PictureUploadDialog.PictureUploadHandler {

    private lateinit var mMap: GoogleMap
    private lateinit var myLocationManager: MyLocationManager

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 200
        public val KEY_PIC_SHOW = "KEY_PIC_SHOW"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestNeededPermission()

        myLocationManager = MyLocationManager(this)
        myLocationManager.startLocationMonitoring()
    }

    var uploadBitmap: Bitmap? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            uploadBitmap = data?.extras?.get(getString(R.string.data)) as Bitmap

            lastMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(uploadBitmap))
        }

        val uploadPicDialog = PictureUploadDialog()

        val bundle = Bundle()
        bundle.putParcelable("Image", uploadBitmap)
        uploadPicDialog.arguments = bundle
        uploadPicDialog.show(supportFragmentManager, "SHOWPIC")
    }

    var lastMarker: Marker? = null

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(47.5, 19.0), 12f))

        mMap.setOnMapClickListener {
            var intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intentCamera, CAMERA_REQUEST_CODE)
            lastMarker = mMap.addMarker(
                MarkerOptions()
                    .position(it)
            )

        }
    }

    fun uploadPost(imageURL: String = "") {
        val post = Post(
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.displayName!!,
            imageURL
        )

//        if (imageURL.isNotEmpty()) {
//            post.imageURL = imageURL
//            Log.d("CHECK URL", post.imageURL)
//        }

        var postsCollection = FirebaseFirestore.getInstance().collection(
            getString(R.string.posts)
        )

        postsCollection.add(post)
            .addOnSuccessListener {
                Toast.makeText(this@MapsActivity, getString(R.string.postsaved), Toast.LENGTH_LONG)
            }
            .addOnFailureListener {
                Toast.makeText(this@MapsActivity, getString(R.string.error), Toast.LENGTH_LONG)
            }
    }

    public fun uploadPhoto() {
        val baos = ByteArrayOutputStream()
        uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().getReference()
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        newImagesRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
                Toast.makeText(this@MapsActivity, exception.message, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                newImagesRef.downloadUrl.addOnCompleteListener(object : OnCompleteListener<Uri> {
                    override fun onComplete(task: Task<Uri>) {
                        uploadPost(task.result.toString())
                        Log.d("PHOTO", task.result.toString())
                    }
                })
            }
    }

    fun showLocation(location: Location) {
        if (location != null) {
            var currentLatLng = LatLng(location.latitude, location.longitude)
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
        }
    }

    override fun onStop() {
        super.onStop()

        myLocationManager.stopLocationMonitoring()
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE
            )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, getString(R.string.permissions), Toast.LENGTH_SHORT).show()
                } else {
                    //Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun pictureUploadShow(image: Bitmap) {

    }
}
