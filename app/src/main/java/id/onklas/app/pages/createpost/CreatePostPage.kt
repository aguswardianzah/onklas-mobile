package id.onklas.app.pages.createpost

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.otaliastudios.autocomplete.Autocomplete
import com.otaliastudios.autocomplete.AutocompleteCallback
import com.otaliastudios.autocomplete.CharPolicy
import com.otaliastudios.autocomplete.RecyclerViewPresenter
import com.yalantis.ucrop.UCrop
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.CreatePostPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.home.dialogs.HomeDialog
import id.onklas.app.pages.sekolah.MediaViewholder
import id.onklas.app.pages.sekolah.sosmed.FeedFileTable
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.SnapOnScrollListener
import id.onklas.app.utils.UsernameAdapter
import kotlinx.coroutines.launch
import java.io.File

class CreatePostPage : Privatepage() {

    private val binding: CreatePostPageBinding by lazy {
        CreatePostPageBinding.inflate(layoutInflater)
    }
    private val viewmodel by viewModels<CreatePostViewmodel> { component.createPostVmFactory }
    private val usernameAdapter by lazy { UsernameAdapter() }

    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val snapHelper by lazy { PagerSnapHelper() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_close)
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
        }

        binding.viewmodel = viewmodel

        binding.btnKamera.setOnClickListener { viewmodel.intentUtil.openCamera(this) }
        binding.btnGalery.setOnClickListener {
            viewmodel.intentUtil.openGalleryPhoto(
                this, "Posting Gambar"
            )
        }

        binding.layoutMedia.mediaIndicator.visibility = View.GONE
        binding.layoutMedia.counter.visibility = View.GONE

        binding.layoutMedia.rvMedia.adapter = mediaAdapter

        viewmodel.feedContent.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                binding.counter.text = "0/500"
                binding.counter.setTextColor(colorGray)
                binding.inputDivider.setBackgroundColor(colorGray)
                binding.btnPost.isEnabled = false
            } else {
                binding.counter.text = "${it.length}/500"
                binding.counter.setTextColor(colorPrimary)
                binding.inputDivider.setBackgroundColor(colorPrimary)
                binding.btnPost.isEnabled = true
            }
        })

        viewmodel.listMedia.observe(this, Observer {
            viewmodel.allowPost.postValue(it.isNotEmpty())
            if (it.isEmpty()) {
                binding.layoutMedia.root.visibility = View.GONE
                binding.layoutMedia.mediaIndicator.visibility = View.GONE
                binding.layoutMedia.counter.visibility = View.GONE

                binding.btnKamera.visibility = View.VISIBLE
                binding.btnGalery.visibility = View.VISIBLE
            } else {
                mediaAdapter.submitList(it) {
                    binding.btnKamera.visibility = View.GONE
                    binding.btnGalery.visibility = View.GONE
                    if (it.size > 1) {
                        binding.layoutMedia.mediaIndicator.visibility = View.VISIBLE
                        binding.layoutMedia.counter.visibility = View.VISIBLE
                        binding.layoutMedia.mediaIndicator.count = it.size
                        try {
                            snapHelper.attachToRecyclerView(binding.layoutMedia.rvMedia)
                            binding.layoutMedia.rvMedia.addOnScrollListener(
                                SnapOnScrollListener(
                                    snapHelper,
                                    SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_STATE_IDLE
                                ) { page ->
                                    binding.layoutMedia.mediaIndicator.selection = page
                                    binding.layoutMedia.counter.text = "${page + 1}/${it.size}"
                                    binding.layoutMedia.counter.visibility = View.VISIBLE
                                })
                        } catch (e: Exception) {
                        }
                    } else {
                        binding.layoutMedia.mediaIndicator.visibility = View.GONE
                        binding.layoutMedia.counter.visibility = View.GONE
                    }
                }
            }
        })

        viewmodel.allowPost.observe(this, Observer {
            binding.btnPost.isEnabled = it
        })

        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.btnPost.setOnClickListener {
            if (viewmodel.feedContent.value.isNullOrEmpty())
                MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                    .setMessage("content tidak boleh kosong")
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .show()
            else
                lifecycleScope.launch {
                    loading(msg = "sedang mengirim post")
                    val createResult = viewmodel.createPost()
                    dismissloading()

                    if (createResult) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
        }

        viewmodel.listUser.observe(this, Observer(usernameAdapter::submitList))
        lifecycleScope.launchWhenCreated {
            viewmodel.listUser.postValue(viewmodel.db.feed().searchUsername())
        }

        Autocomplete.on<UserTable>(binding.inputContent)
            .with(1f)
            .with(ColorDrawable(Color.WHITE))
            .with(CharPolicy('@'))
            .with(object : RecyclerViewPresenter<UserTable>(this) {
                override fun onQuery(query: CharSequence?) {
                    lifecycleScope.launchWhenCreated {
                        viewmodel.searchUsername(query.toString())
                    }
                }

                override fun instantiateAdapter(): RecyclerView.Adapter<*> =
                    usernameAdapter.apply { onClick = { item -> dispatchClick(item) } }
            })
            .with(object : AutocompleteCallback<UserTable> {
                override fun onPopupItemClicked(editable: Editable?, item: UserTable?): Boolean {
                    item?.let {
                        val range = CharPolicy.getQueryRange(editable) ?: return false
                        val start = range[0]
                        val end = range[1]
                        val replacement = if (it.username.isEmpty()) it.name else it.username
                        editable?.apply {
                            replace(start, end, replacement)
                            setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                start + (replacement.length),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    return true
                }

                override fun onPopupVisibilityChanged(shown: Boolean) {}
            })
            .build()

        binding.btnDeleteImage.setOnClickListener {
            viewmodel.listMedia.postValue(arrayListOf())
            binding.btnDeleteImage.visibility = View.GONE
        }

        if (viewmodel.user.value?.username.isNullOrEmpty())
            HomeDialog.showSetUsernameDialog(this, supportFragmentManager, layoutInflater)

        binding.executePendingBindings()
    }

    private val glide by lazy { GlideApp.with(this) }
    private val mediaAdapter by lazy {
        object :
            ListAdapter<FeedFileTable, MediaViewholder>(object :
                DiffUtil.ItemCallback<FeedFileTable>() {
                override fun areItemsTheSame(
                    oldItem: FeedFileTable,
                    newItem: FeedFileTable
                ): Boolean =
                    oldItem.path == newItem.path

                override fun areContentsTheSame(
                    oldItem: FeedFileTable,
                    newItem: FeedFileTable
                ): Boolean =
                    oldItem.path == newItem.path
            }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewholder =
                MediaViewholder(parent)

            override fun onBindViewHolder(holder: MediaViewholder, position: Int) =
                holder.bind(getItem(position), glide = glide)
        }
    }

    private val options by lazy { BitmapFactory.Options().apply { inJustDecodeBounds = true } }
    fun getFileImageSize(path: String): Pair<Int, Int> {
        val bitmap = BitmapFactory.decodeFile(path, options)
        return options.outWidth to options.outHeight
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IntentUtil.RC_GALLERY_PHOTO) {
            data?.data?.let { imageEditor(it) }

//            val uri =
//                data?.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
//            if (!uri.isNullOrEmpty()) {
//                imageEditor(uri.first())

//                ContentUriUtils.getFilePath(this, uri.first())?.let {
//                    Timber.e("file cover: $it")
//
//                    lifecycleScope.launch {
//                        loading(msg = "memproses gambar")
//                        val bitmapfromuri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                            ImageDecoder.decodeBitmap(
//                                ImageDecoder.createSource(
//                                    contentResolver, uri.first()
//                                )
//                            )
//                        } else BitmapFactory.decodeFile(it)
//
//                        val bitmap = viewmodel.fileUtils.resizeBitmap(
//                            bitmapfromuri,
//                            680,
//                            720
//                        )
//                        val file = File(filesDir, "cover.jpg").apply { createNewFile() }
//                        if (viewmodel.fileUtils.bitmapToFile(bitmap, file)) {
//                            viewmodel.feed_thumb.postValue(Uri.fromFile(file).toString())
//                        }
//                        viewmodel.listMedia.postValue(
//                            arrayListOf(
//                                FeedFileTable(
//                                    0,
//                                    0,
//                                    Uri.fromFile(file).toString(),
//                                    width = bitmap.width,
//                                    height = bitmap.height
//                                ).also { Timber.e("added file: $it") })
//                        )
//                        dismissloading()
//                    }
//                }
//            }
        } else if (requestCode == IntentUtil.RC_CAMERA && resultCode == RESULT_OK) {
            imageEditor(Uri.fromFile(File(viewmodel.intentUtil.currentPhotoPath)))
//            val size = getFileImageSize(viewmodel.intentUtil.currentPhotoPath)
//            viewmodel.listMedia.postValue(
//                arrayListOf(
//                    FeedFileTable(
//                        0,
//                        0,
//                        viewmodel.intentUtil.currentPhotoPath,
//                        width = size.first,
//                        height = size.second
//                    )
//                )
//            )
//            data?.getStringExtra("fileUri")?.let {
//                viewmodel.listMedia.postValue(arrayListOf(FeedFileTable(0, 0, it)))
//            }
        } else if (requestCode == UCrop.REQUEST_CROP && data != null) {
            UCrop.getOutput(data)?.let { uri ->
                viewmodel.listMedia.postValue(
                    arrayListOf(
                        FeedFileTable(
                            0,
                            0,
                            uri.path.toString(),
                            width = binding.layoutMedia.rvMedia.width,
                            height = binding.layoutMedia.rvMedia.width
                        )
                    )
                )
                binding.btnDeleteImage.visibility = View.VISIBLE
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private val colorTextBlack by lazy { ContextCompat.getColor(this, R.color.textBlack) }
    private fun imageEditor(uri: Uri) {
        UCrop.of(uri, Uri.fromFile(File(cacheDir, "${System.currentTimeMillis()}.jpg")))
            .withOptions(UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(80)
                setShowCropGrid(false)
                withMaxResultSize(720, 720)
                withAspectRatio(1f, 1f)
                setDimmedLayerColor(Color.parseColor("#99FFFFFF"))
                setRootViewBackgroundColor(Color.WHITE)
                setShowCropFrame(false)
                setStatusBarColor(Color.WHITE)
                setToolbarColor(Color.WHITE)
                setActiveControlsWidgetColor(colorPrimary)
                setToolbarWidgetColor(colorTextBlack)
                setToolbarTitle("Atur Gambar")
            })
            .start(this)
    }
}