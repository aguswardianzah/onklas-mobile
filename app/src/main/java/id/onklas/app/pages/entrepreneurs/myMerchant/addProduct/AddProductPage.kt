package id.onklas.app.pages.entrepreneurs.myMerchant.addProduct

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yalantis.ucrop.UCrop
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.IntentUtil
import timber.log.Timber
import java.io.File

class AddProductPage : Privatepage() {

    private val binding by lazy { AddProductPage2Binding.inflate(layoutInflater) }

    //    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val viewModel by viewModels<AddProductViewModel> { component.addProductVmFactory }
    private val dimen16 by lazy { resources.getDimensionPixelSize(R.dimen._16sdp) }
    private val productId by lazy { intent.getIntExtra("productId", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        if (productId > 0) {
            viewModel.isLoading.observe(this) {
                if (it)
                    loading(msg = viewModel.loadingMsg.value.orEmpty())
                else
                    dismissloading()
            }

            viewModel.errorLoadingDetail.observe(this) {
                if (it)
                    alert(msg = "Gagal menampilkan data produk", okClick = { finish() })
            }

            viewModel.loadDetailProduct(productId)
        }

        binding.lifecycleOwner = this
        binding.viewModels = viewModel

        binding.labelName.setRequired()
        binding.labelDesc.setRequired()
        binding.labelPrice.setRequired()
        binding.labelStock.setRequired()
        binding.labelCategory.setRequired()

        binding.rvImages.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)

                outRect.right = dimen16 / 2

                val position = parent.getChildAdapterPosition(view)
                if (position == 0)
                    outRect.left = dimen16

                parent.adapter?.let {
                    if (position == it.itemCount - 1)
                        outRect.right = dimen16
                }
            }
        })
        binding.rvImages.adapter = imgAdapter

        viewModel.productImages.observe(this, imgAdapter::submitList)

        binding.tvDesc.setOnClickListener {
            startActivityForResult(
                Intent(this, ProductDescriptionPage::class.java).putExtra(
                    "desc",
                    viewModel.productDesc.value
                ), 281
            )
        }

        binding.actionCategory.setOnClickListener { openCategory() }
        binding.labelCategory.setOnClickListener { openCategory() }
        binding.tvCategory.setOnClickListener { openCategory() }

        binding.btnSave.setOnClickListener {
            if (viewModel.productImages.value?.isEmpty() == true)
                toast("mohon isi gambar produk terlebih dahulu")
            else if (!viewModel.allowSave())
                toast("mohon lengkapi data yang diperlukan")
            else {
                viewModel.saveProduct()

                viewModel.isSuccessSave.observe(this) {
                    if (it) {
                        toast("berhasil menyimpan produk")
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
        }

        binding.executePendingBindings()

//        binding.actionMarketingLocation.setOnClickListener {
//            startActivity(Intent(this, AddProductMarketingLocationPage::class.java))
//        }
//        binding.actionSendMethode.setOnClickListener {
//            startActivity(Intent(this, AddProductMetodePengiriman::class.java))
//        }
    }

    private fun openCategory() {
        startActivityForResult(Intent(this, AddProductCategoryListPage::class.java), 293)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 281 && resultCode == RESULT_OK) {
            viewModel.productDesc.postValue(data?.getStringExtra("desc"))
        } else if (requestCode == IntentUtil.RC_GALLERY_PHOTO) {
            data?.data?.let { imageEditor(it) }
//            val uri =
//                data?.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
//            if (!uri.isNullOrEmpty()) {
//                imageEditor(uri.first())
//            }
        } else if (requestCode == UCrop.REQUEST_CROP && data != null) {
            UCrop.getOutput(data)?.let { uri ->
                val list = (viewModel.productImages.value ?: mutableListOf()).toMutableList()
                val changeIndex =
                    if (imageIndex > -1) imageIndex else list.indexOfFirst { it.isEmpty() }
                viewModel.updateImage(changeIndex, uri.path.toString())
            }
        } else if (requestCode == 293 && resultCode == RESULT_OK) {
            viewModel.productCategoryParent.postValue(data?.getIntExtra("parentId", 0))
            viewModel.productCategory.postValue(data?.getIntExtra("categoryId", 0))
            viewModel.productCategoryName.postValue(data?.getStringExtra("categoryName").orEmpty())
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val colorTextBlack by lazy { ContextCompat.getColor(this, R.color.textBlack) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private fun imageEditor(uri: Uri) {
        Timber.e("uri: $uri")
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

    private val imgAdapter by lazy {
        object : ListAdapter<String, ImgAddVH>(object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = false

            override fun getChangePayload(oldItem: String, newItem: String): Any = newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImgAddVH =
                ImgAddVH(parent)

            override fun onBindViewHolder(holder: ImgAddVH, position: Int) =
                holder.bind(getItem(position))

            override fun onBindViewHolder(
                holder: ImgAddVH,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty())
                    holder.update(payloads.first() as String)
                else
                    onBindViewHolder(holder, position)
            }
        }
    }

    private var imageIndex = -1

    private inner class ImgAddVH(
        parent: ViewGroup,
        val binding: AddProductItemBinding = AddProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.clipToOutline = true
        }

        fun bind(image: String) {
            binding.root.setOnClickListener {
                imageIndex = bindingAdapterPosition
                if (image.isEmpty())
                    viewModel.intentUtil.openGalleryPhoto(this@AddProductPage, "Gambar Produk")
                else
                    showOptions()
            }

            update(image)
        }

        private fun showOptions() {
            alertSelect(
                "Atur Gambar",
                items = arrayOf("Hapus Gambar", "Ganti Gambar")
            ) { pos ->
                if (pos == 0) {
                    alert(
                        "Hapus Gambar",
                        "Anda yakin akan menghapus gambar?",
                        "Hapus",
                        { viewModel.deleteImage(bindingAdapterPosition) },
                        "Batal"
                    )
                } else {
                    imageIndex = bindingAdapterPosition
                    viewModel.intentUtil.openGalleryPhoto(
                        this@AddProductPage,
                        "Gambar Produk"
                    )
                }
            }
        }

        fun update(image: String) {
            binding.image = image
            binding.executePendingBindings()
        }
    }

    private fun TextView.setRequired() {
        val text = this.text.toString()
        val indexAsterick = text.lastIndexOf('*')
        if (indexAsterick < text.length && indexAsterick > -1)
            this.text = SpannableStringBuilder(text).apply {
                setSpan(
                    ForegroundColorSpan(colorPrimary),
                    indexAsterick,
                    kotlin.math.min(indexAsterick + 1, text.length),
                    0
                )
            }
    }
}