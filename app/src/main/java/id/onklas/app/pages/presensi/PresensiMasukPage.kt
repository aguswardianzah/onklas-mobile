package id.onklas.app.pages.presensi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import id.onklas.app.R
import id.onklas.app.databinding.PresensiMasukDialogBinding
import id.onklas.app.databinding.PresensiMasukPageBinding
import id.onklas.app.databinding.PresensiSuccessDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class PresensiMasukPage : Privatepage() {

    private val binding by lazy { PresensiMasukPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PresensiViewModel> { component.presensiVmFactory }

    //    private val myLocation by lazy {
//        MyLocationNewOverlay(GpsMyLocationProvider(this), binding.map).apply {
//            enableMyLocation()
//        }
//    }

    private lateinit var map: GoogleMap
    private val myLocMarker by lazy {
        MarkerOptions().title("my location")
    }
    private var myLocAttached = false

    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var locationRunning = false
    private val profileImgLoaded by lazy { MutableLiveData(false) }

    init {
        lifecycleScope.launchWhenCreated {
            val byteArray =
                viewmodel.apiService.download(viewmodel.userTable.user_avatar_image).bytes()
            val iconSize = resources.getDimensionPixelSize(R.dimen._24sdp)
            val bitmap = viewmodel.utils.circleCropBitmap(
                Bitmap.createScaledBitmap(
                    BitmapFactory.decodeByteArray(
                        byteArray, 0, byteArray.size
                    ), iconSize, iconSize, false
                )
            )
            myLocMarker.icon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
            profileImgLoaded.postValue(true)

            viewmodel.checkAbsent()
            dismissloading()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        Configuration.getInstance().load(
//            applicationContext,
//            PreferenceManager.getDefaultSharedPreferences(applicationContext)
//        )

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        binding.lifecycleOwner = this
        binding.userTable = viewmodel.userTable
        handler.postDelayed(timer, 0)

        lifecycleScope.launchWhenCreated {
            viewmodel.db.schedule().getLastAbsensi()?.let {
                binding.lastAbsen =
                    "${it.dateLabel}, ${it.leave_at.ifEmpty { it.attend_at }}"
            } ?: run {
                binding.lastAbsen = ""
            }
        }

//        Configuration.getInstance().userAgentValue = "github-glenn1wang-myapp"
//        binding.map.apply {
//            setTileSource(TileSourceFactory.MAPNIK)
//            setMultiTouchControls(true)
//            controller.setZoom(16.0)
//            overlays.add(myLocation)
//        }

        Dexter.withContext(this).withPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).withListener(object : MultiplePermissionsListener {
            @SuppressLint("MissingPermission")
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    Timber.e("location permission granted")
                    initLocationRequest()
                } else alert(msg = "Absensi tidak dapat dilakukan tanpa informasi lokasi Anda") { finish() }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }
        }).check()

        binding.btnMyLoc.setOnClickListener {
            lastLocation?.let {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude), 16.0F
                    )
                )
            }
        }

        viewmodel.allowAbsent.observe(this, Observer(binding::setAllowChecklog))

        loading(msg = "menampilkan data")
        viewmodel.absenType.observe(this) {
            binding.typeChecklog = it
            dismissloading()
        }

        viewmodel.stringError.observe(this, Observer(this::toast))

        viewmodel.buttonLabel.observe(this, Observer(binding::setButtonLabel))
        viewmodel.errorLabel.observe(this, Observer(binding::setErrorLabel))

        binding.btnAbsen.setOnClickListener {
            val dialogBinding = PresensiMasukDialogBinding.inflate(layoutInflater).apply {
                message.text =
                    "Waktu tercatat saat Anda melakukan absensi, Anda yakin ${viewmodel.absenType.value} absensi sekarang"
            }

            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

            dialogBinding.btnAbsen.setOnClickListener {
                lifecycleScope.launch {
                    dialog.dismiss()
                    lastLocation?.let {
                        loading(msg = "memproses absensi")
                        val res = viewmodel.doAbsensi(it.latitude, it.longitude)
                        dismissloading()
                        if (res) {
                            val successDialogBinding =
                                PresensiSuccessDialogBinding.inflate(layoutInflater).apply {
                                    message.text =
                                        "Pencatatan waktu ${viewmodel.absenType.value} Anda telah tersimpan, ${if (viewmodel.absenType.value == "Masuk") "silahkan melanjutkan jadwal hari ini" else "terima kasih"}"
                                }
                            val successDialog = AlertDialog.Builder(this@PresensiMasukPage)
                                .setView(successDialogBinding.root)
                                .show()
                                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                            successDialogBinding.btnAbsen.setOnClickListener {
                                successDialog.dismiss()
                                setResult(RESULT_OK)
                                finish()
                            }
                        }
                    } ?: alert(msg = "Gagal mendapatkan lokasi Anda")
                }
            }
            dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        }

        loading(msg = "menampilkan data")

        binding.executePendingBindings()
    }

    private lateinit var locationRequest: LocationRequest
    private fun initLocationRequest() {
        LocationRequest.create()
            .apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            .let {
                this.locationRequest = it
                val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(it)

                val client = LocationServices.getSettingsClient(this@PresensiMasukPage)
                client.checkLocationSettings(builder.build())
                    .addOnSuccessListener {
                        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { googleMap ->
                            map = googleMap
                        }
                        getUpdateLocation()
                    }
                    .addOnFailureListener { exception ->
                        if (exception is ResolvableApiException) {
                            // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                                exception.startResolutionForResult(
                                    this@PresensiMasukPage,
                                    423
                                )
                            } catch (sendEx: IntentSender.SendIntentException) {
                                toast("Gagal mendapatkan lokasi")
                            }
                        }
                    }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 423) {
            initLocationRequest()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                Timber.e("get update location")
                result.lastLocation?.let { setLocation(it) }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)

                Timber.e("location available: ${locationAvailability.isLocationAvailable}")
                if (!locationAvailability.isLocationAvailable) {
                    locationRunning = false
                    getLastLocation()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUpdateLocation() {
        try {
            // try to get location updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            locationRunning = true
        } catch (e: Exception) {
            alert(msg = "Gagal mendapatkan informasi lokasi Anda") { finish() }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        // try to get last location
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                Timber.e("get last location")
                location?.let { setLocation(it) }
            }.addOnFailureListener {
                alert(msg = "Gagal mendapatkan informasi lokasi Anda") { finish() }
            }
        } catch (e: Exception) {
            alert(msg = "Gagal mendapatkan informasi lokasi Anda") { finish() }
        }
    }

    private var lastLocation: Location? = null
    private fun setLocation(location: Location) {
        Timber.e("set location: ${location.latitude}, ${location.longitude}")
        lastLocation = location
        val latlng = LatLng(location.latitude, location.longitude)
        myLocMarker.position(latlng)
        profileImgLoaded.removeObservers(this)
        profileImgLoaded.observe(this) {
            if (it && !myLocAttached) {
                map.addMarker(myLocMarker)
            }
        }

        if (!myLocAttached) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.0F))
            myLocAttached = true
        }
//        binding.map.post {
//            binding.map.controller.setCenter(
//                GeoPoint(location.latitude, location.longitude)
//            )
//        }
    }

    override fun onResume() {
        super.onResume()

        handler.postDelayed(timer, 0)
        if (locationRunning) getUpdateLocation()
    }

    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(timer)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        handler.removeCallbacks(timer)
        super.onDestroy()
    }

    private val handler by lazy { Handler(mainLooper) }
    private val timeFormat by lazy { SimpleDateFormat("HH:mm:ss", Locale("id")) }
    private val timer: Runnable by lazy {
        Runnable {
            binding.dateLabel = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id")).format(Date())
            binding.timeLabel = timeFormat.format(Date())
            binding.executePendingBindings()
            handler.postDelayed(timer, 1000)
        }
    }
}