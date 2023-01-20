package id.onklas.app.utils

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.LabeledIntent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.linkdev.filepicker.factory.PickFilesFactory
import com.linkdev.filepicker.models.FileTypes
import com.linkdev.filepicker.models.MimeType
import com.linkdev.filepicker.models.SelectionMode
import droidninja.filepicker.FilePickerBuilder
import id.onklas.app.BuildConfig
import id.onklas.app.R
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.PersistentDB
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.theory.PdfPage
import id.onklas.app.socket.SocketClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject


class
IntentUtil @Inject constructor(
    val context: Context,
    val memoryDB: MemoryDB,
    val persistentDB: PersistentDB,
    val pref: PreferenceClass,
    val fileUtils: FileUtils,
    val apiService: ApiService,
    val socketClass: SocketClass
) {

    fun openWhatsApp(
        activity: Activity,
        phone: String,
        content: String = "",
        onFailed: () -> Unit = {}
    ) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.whatsapp")
                data = Uri.parse("https://wa.me/$phone?text=$content")
            })
        } catch (e: Exception) {
            Timber.e(e)
            onFailed.invoke()
        }
    }

    fun openCall(activity: Activity, phone: String) {
        activity.startActivity(Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel: $phone")
        })
    }

    fun openSms(activity: Activity, phone: String, content: String = "") {
        activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms: $phone")
            putExtra("sms_body", content)
        })
    }

    var onChoosenIntent: (Intent?) -> Unit = {}
    private val chooserReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context?.unregisterReceiver(this)
                try {
                    onChoosenIntent.invoke(intent)
                    intent?.extras?.let {
                        for (key in it.keySet()) {
                            Timber.e("broadcast intent: %s", it[key])
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    fun openEmail(activity: Activity, onChoosenIntent: (Intent?) -> Unit = {}) {
        val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
        val packageManager = activity.packageManager

        val activitiesHandlingEmails = packageManager.queryIntentActivities(emailIntent, 0)
        if (activitiesHandlingEmails.isNotEmpty()) {
            // use the first email package to create the chooserIntent
            val firstEmailPackageName = activitiesHandlingEmails.first().activityInfo.packageName
            val firstEmailInboxIntent =
                packageManager.getLaunchIntentForPackage(firstEmailPackageName)
            val emailAppChooserIntent =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Intent.createChooser(
                        firstEmailInboxIntent,
                        "Buka email dengan",
                        PendingIntent.getBroadcast(
                            activity,
                            0,
                            Intent("${activity.packageName}.CHOOSER_INTENT"),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        ).intentSender
                    )
                } else {
                    Intent.createChooser(
                        firstEmailInboxIntent,
                        "Buka email dengan"
                    )
                }

            // created UI for other email packages and add them to the chooser
            val emailInboxIntents = mutableListOf<LabeledIntent>()
//            for (i in 1 until activitiesHandlingEmails.size) {
//                val activityHandlingEmail = activitiesHandlingEmails[i]
            for (activityHandlingEmail in activitiesHandlingEmails) {
                val packageName = activityHandlingEmail.activityInfo.packageName
                Timber.e("package: $packageName")
                if (emailInboxIntents.firstOrNull { it.sourcePackage == packageName } == null) {
                    emailInboxIntents.add(
                        LabeledIntent(
                            packageManager.getLaunchIntentForPackage(packageName),
                            packageName,
                            activityHandlingEmail.loadLabel(packageManager),
                            activityHandlingEmail.icon
                        )
                    )
                }
            }
            val extraEmailInboxIntents = emailInboxIntents.toTypedArray()
            Timber.e("extraEmailInboxIntents: $extraEmailInboxIntents")

            if (extraEmailInboxIntents.isNotEmpty()) {
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                    this.onChoosenIntent = onChoosenIntent
//                    LocalBroadcastManager.getInstance(activity).registerReceiver(
//                        chooserReceiver,
//                        IntentFilter("${activity.packageName}.CHOOSER_INTENT")
//                    )
//                } else
                onChoosenIntent.invoke(null)

                activity.startActivity(
                    emailAppChooserIntent.putExtra(
                        Intent.EXTRA_INITIAL_INTENTS,
                        extraEmailInboxIntents
                    )
                )
            } else {
                onChoosenIntent.invoke(null)
                Timber.e("email intent is empty")
            }
        } else {
            onChoosenIntent.invoke(null)
            Timber.e("mailto intent is empty")
        }
    }

    var currentPhotoPath: String = ""
    fun openCamera(activity: Activity) {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT <= 28)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                            // Ensure that there's a camera activity to handle the intent
                            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                                // Create the File where the photo should go
                                val photoFile: File? = try {
                                    val timeStamp: String = System.currentTimeMillis().toString()
                                    val storageDir: File =
                                        activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                            ?: activity.filesDir
                                    File.createTempFile(
                                        "JPEG_${timeStamp}_", /* prefix */
                                        ".jpg", /* suffix */
                                        storageDir /* directory */
                                    ).apply {
                                        // Save a file: path for use with ACTION_VIEW intents
                                        currentPhotoPath = absolutePath
                                    }
                                } catch (ex: IOException) {
                                    (activity as? BasePage)?.toast("Gagal membuka kamera, ijin untuk menyimpan file tidak diberikan")
                                    null
                                }
                                // Continue only if the File was successfully created
                                photoFile?.also {
                                    val photoURI: Uri = FileProvider.getUriForFile(
                                        activity,
                                        BuildConfig.APPLICATION_ID,
                                        it
                                    )
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    activity.startActivityForResult(takePictureIntent, RC_CAMERA)
                                }
                            }
                        }

//                        activity.startActivityForResult(
//                            Intent(activity, CameraPage::class.java),
//                            RC_CAMERA
//                        )
                    } else {
                        (activity as? BasePage)?.toast("Gagal membuka kamera, ijin untuk menyimpan file tidak diberikan")
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    fun openGalleryPhoto(activity: Activity, title: String) {
        val permissions = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT <= 28)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {

                        activity.startActivityForResult(
                            Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            ), RC_GALLERY_PHOTO
                        )

//                        FilePickerBuilder.instance
//                            .setMaxCount(1)
//                            .setActivityTitle(title)
//                            .setActivityTheme(R.style.PickerTheme)
//                            .enableCameraSupport(false)
//                            .showFolderView(false)
//                            .pickPhoto(activity, RC_GALLERY_PHOTO)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    fun downloadFile(activity: Activity, fileUri: Uri, fileName: String, fileType: String) {
        val permissions = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT <= 28)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        if (fileUri.toString().isEmpty())
                            Toast.makeText(
                                activity,
                                "$fileType tidak tersedia, mohon ulangi beberapa saat lagi",
                                Toast.LENGTH_SHORT
                            ).show()
                        else {
                            Toast.makeText(
                                activity,
                                "proses download $fileType akan dimulai sesaat lagi",
                                Toast.LENGTH_SHORT
                            ).show()
                            (activity.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager?)?.enqueue(
                                DownloadManager.Request(fileUri)
                                    .apply {
                                        setTitle(fileName)
                                        setDestinationInExternalPublicDir(
                                            Environment.DIRECTORY_DOWNLOADS, fileName
                                        )
                                        allowScanningByMediaScanner()
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    }
                            )
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    fun openPdfPicker(activity: Activity, pageTitle: String) = openFilePicker(
        activity,
        pageTitle,
        listOf(Triple("Pdf", arrayOf("pdf"), R.drawable.ic_pdf))

    )

    fun openFilePicker(
        activity: Activity,
        pageTitle: String,
        fileType: List<Triple<String, Array<String>, Int>>
    ) {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT <= 28)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    FilePickerBuilder.instance
                        .setMaxCount(1)
                        .setActivityTitle(pageTitle)
                        .setActivityTheme(R.style.PickerTheme)
                        .apply {
                            fileType.forEach {
                                addFileSupport(it.first, it.second, it.third)
                            }
                        }
                        .enableDocSupport(false)
                        .showFolderView(true)
                        .enableImagePicker(true)
//                        .apply {
//                            fileType.firstOrNull { it.second.contains("jpg") }?.let {
//                                enableImagePicker(true)
//                            }
//                        }
                        .pickFile(activity, RC_PDF_PICKER)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    fun openFile(activity: Activity, file: File, title: String) = try {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.let {
            Timber.e("file type: $it")
            if (it.contains("pdf", true)) {
                activity.startActivity(
                    Intent(activity, PdfPage::class.java)
                        .putExtra("file_path", file.path)
                        .putExtra("title", title)
                )
            } else {
                val intent = Intent()
                    .setAction(Intent.ACTION_VIEW)

                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID, file)
                } else
                    Uri.fromFile(file)

                intent.setDataAndType(uri, it)
                activity.startActivity(intent)
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
        (activity as? BasePage)?.toast("Gagal membuka file")
    }

    fun openPdf(activity: Activity, uri: String, title: String) {
//        try {
            activity.startActivity(
                Intent(activity, PdfPage::class.java)
                    .putExtra("file_path", uri)
                    .putExtra("title", title)
            )
//            val file = File(uri)
//            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.let {
//                if (it.contains("pdf", true)) {
//                    activity.startActivity(
//                        Intent(activity, PdfPage::class.java)
//                            .putExtra("file_path", uri)
//                            .putExtra("title", title)
//                    )
//                } else {
//                    val intent = Intent()
//                        .setAction(Intent.ACTION_VIEW)
//                        .setData(Uri.parse(uri))
////                            .setDataAndType(Uri.parse(uri), it)
////                            .setDataAndType(Uri.fromFile(file), it)
//                        .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//                    if (intent.resolveActivity(activity.packageManager) != null)
//                        activity.startActivity(intent)
//                    else
//                        (activity as? BasePage)?.toast("Gagal membuka file, tidak terdapat aplikasi yang sesuai")
//                }
//            }
//        } catch (e: Exception) {
//            Timber.e(e)
//            (activity as? BasePage)?.toast("Gagal membuka file")
//
////            val intent = Intent()
////                .setAction(Intent.ACTION_VIEW)
////                .setData(Uri.parse(uri))
////                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION)
////
////            if (intent.resolveActivity(activity.packageManager) != null)
////                activity.startActivity(intent)
//        }
    }

    fun copyText(activity: Activity, text: String) {
        (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("label", text))
    }

    suspend fun logOut() {
        try {
            withContext(Dispatchers.IO) {
                memoryDB.clearAllTables()
                persistentDB.clearAllTables()
            }
            FirebaseMessaging.getInstance().apply {
                unsubscribeFromTopic("onklas-notification-user-${pref.getString("user_uuid")}")
                unsubscribeFromTopic("Klaspay-US-${pref.getInt("user_id")}")
                unsubscribeFromTopic("attendance-${pref.getInt("class_id")}")
                unsubscribeFromTopic("loggedin")

                subscribeToTopic("unlogged")
            }
            try {
                if (pref.getBoolean("logged_in")) apiService.logout()
            } catch (e: Exception) {
                Timber.e(e)
            }
            pref.edit(true) {
                clear()
                putString("url_api", BuildConfig.API_URL)
                putBoolean("onboard", true)
            }

            socketClass.disconnect()

            NotificationManagerCompat.from(context).cancelAll()
            WorkManager.getInstance(context).cancelAllWork()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun logOut(context: Context) {
        GlobalScope.launch {
            memoryDB.clearAllTables()
            persistentDB.clearAllTables()
            FirebaseMessaging.getInstance().apply {
                unsubscribeFromTopic("onklas-notification-user-${pref.getString("user_uuid")}")
                unsubscribeFromTopic("Klaspay-US-${pref.getInt("user_id")}")
                unsubscribeFromTopic("attendance-${pref.getInt("class_id")}")
                unsubscribeFromTopic("loggedin")

                subscribeToTopic("unlogged")
            }
            try {
                if (pref.getBoolean("logged_in")) apiService.logout()
            } catch (e: Exception) {
                Timber.e(e)
            }
            pref.edit(true) {
                clear()
                putString("url_api", BuildConfig.API_URL)
                putBoolean("onboard", true)
                putBoolean("check_vc", true)
            }

            socketClass.disconnect()

            NotificationManagerCompat.from(context.applicationContext).cancelAll()
            WorkManager.getInstance(context).cancelAllWork()
        }
    }

    fun openFilePicker2(
        activity: Activity,
        title: String = "Pilih File",
        mimeTypes: ArrayList<MimeType> = arrayListOf(MimeType.ALL_IMAGES, MimeType.PDF),
        rc: Int = RC_FILE_PICKER
    ) =
        PickFilesFactory(
            caller = activity,
            requestCode = rc,
            galleryFolderName = title,
            allowSyncWithGallery = true,
            selectionMode = SelectionMode.SINGLE
        ).getInstance(FileTypes.PICK_FILES)
            ?.also {
                val permissions = mutableListOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if (Build.VERSION.SDK_INT <= 28)
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                Dexter.withContext(activity)
                    .withPermissions(permissions)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            it.pickFiles(mimeTypes)
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }
                    })
                    .check()
            }

    companion object {
        const val RC_PDF_PICKER = 8329
        const val RC_CAMERA = 4382
        const val RC_GALLERY_PHOTO = 1321
        const val RC_FILE_PICKER = 2020
    }
}