package id.onklas.app.pages.magang

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
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
import id.onklas.app.databinding.MagangAttendPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment
import kotlinx.coroutines.launch
import timber.log.Timber

class MagangAttendPage(val scheduleId: Int, val onDismiss: () -> Unit): PageDialogFragment() {

    private lateinit var binding: MagangAttendPageBinding
    private val viewModel by activityViewModels<MagangViewModel> { component.magangVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = MagangAttendPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        lifecycleScope.launchWhenCreated {
            loading(msg = "menampilkan data")
            viewModel.getJadwal(scheduleId)

            val byteArray =
                viewModel.apiService.download(viewModel.userTable.user_avatar_image).bytes()
            val iconSize = resources.getDimensionPixelSize(R.dimen._24sdp)
            val bitmap = viewModel.utils.circleCropBitmap(
                Bitmap.createScaledBitmap(
                    BitmapFactory.decodeByteArray(
                        byteArray, 0, byteArray.size
                    ), iconSize, iconSize, false
                )
            )
            myLocMarker.icon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
            profileImgLoaded.postValue(true)

            dismissloading()

            Dexter.withContext(requireContext()).withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        Timber.e("location permission granted")
                        initLocationRequest()
                    } else alert(msg = "Absensi tidak dapat dilakukan tanpa informasi lokasi Anda") { dismiss() }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
        }

        viewModel.currSchedule.observe(viewLifecycleOwner, binding::setSchedule)

        binding.btnMyLoc.setOnClickListener {
            lastLocation?.let {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude), 16.0F
                    )
                )
            }
        }

        binding.btnConfirm.setOnClickListener {
            lifecycleScope.launch {
                lastLocation?.let {
                    loading(title = "memulai kelas")

                    val res = viewModel.attend(scheduleId, it)
                    dismissloading()
                    if (res) {
                        onDismiss.invoke()
                        dismiss()
                    }
                } ?: alert(msg = "Gagal mendapatkan lokasi Anda")
            }
        }
    }

    private lateinit var map: GoogleMap
    private val myLocMarker by lazy {
        MarkerOptions().title("my location")
    }
    private var myLocAttached = false

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            requireActivity()
        )
    }
    private var locationRunning = false
    private val profileImgLoaded by lazy { MutableLiveData(false) }

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

                val client = LocationServices.getSettingsClient(requireActivity())
                client.checkLocationSettings(builder.build())
                    .addOnSuccessListener {
                        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { googleMap ->
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
                                    requireActivity(),
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
            alert(msg = "Gagal mendapatkan informasi lokasi Anda") { dismiss() }
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
                alert(msg = "Gagal mendapatkan informasi lokasi Anda") { dismiss() }
            }
        } catch (e: Exception) {
            alert(msg = "Gagal mendapatkan informasi lokasi Anda") { dismiss() }
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
    }

    override fun onResume() {
        super.onResume()

        if (locationRunning) getUpdateLocation()
    }

    override fun onPause() {
        super.onPause()

        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}