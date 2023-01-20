package id.onklas.app.pages.perpus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.SnapOnScrollListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class PerpusPage : Privatepage() {

    private val binding by lazy { PerpusPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PerpusViewModel> { component.perpusVmFactory }
    private val snapHelper by lazy { PagerSnapHelper() }
    private val listSection by lazy { ArrayList<BookSection>() }
    private val bookItemDecor by lazy {
        LinearSpaceDecoration(
            OrientationHelper.HORIZONTAL,
            resources.getDimensionPixelSize(R.dimen._4sdp),
            includeTop = true,
            includeBottom = true,
            resources.getDimensionPixelSize(R.dimen._16sdp)
        )
    }
    private val rvPool by lazy { RecyclerView.RecycledViewPool() }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.initBanner()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.searchLayout.inputSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP)
                startActivity(Intent(this, BookSearchPage::class.java))
            false
        }

        binding.rvBanner.apply {
            adapter = bannerAdapter
            snapHelper.attachToRecyclerView(this)
            addOnScrollListener(
                SnapOnScrollListener(
                    snapHelper, onSnapPositionChangeListener = (binding.headIndicator::setSelection)
                )
            )
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
        viewmodel.listBanner.observe(this, {
            bannerAdapter.submitList(it) {
                binding.headIndicator.count = it.size
                binding.rvBanner.scrollToPosition(0)
            }
        })

        binding.rvBooks.adapter = bookAdapter
        binding.rvBooks.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._12sdp
                )
            )
        )

        lifecycleScope.launch {
            viewmodel.sections.collect {
                if (it.id == 0)
                    listSection.clear()

                listSection.add(it)
                bookAdapter.notifyItemInserted(it.id)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_perpus, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, BookHistoryPage::class.java))
        return true
    }

    private val bannerAdapter by lazy {
        object : ListAdapter<PerpusBanner, BannerViewholder>(object :
            DiffUtil.ItemCallback<PerpusBanner>() {
            override fun areItemsTheSame(oldItem: PerpusBanner, newItem: PerpusBanner): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: PerpusBanner, newItem: PerpusBanner): Boolean =
                areItemsTheSame(oldItem, newItem)
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewholder =
                BannerViewholder(parent)

            override fun onBindViewHolder(holder: BannerViewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class BannerViewholder(
        parent: ViewGroup,
        val binding: PembelajaranBannerBinding = PembelajaranBannerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.clipToOutline = true
        }

        fun bind(item: PerpusBanner) {
            binding.item = item.file_path
            binding.executePendingBindings()
        }
    }

    private val bookAdapter by lazy {
        object : RecyclerView.Adapter<BookSectionViewHolder>() {

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BookSectionViewHolder = BookSectionViewHolder(parent)

            override fun onBindViewHolder(holder: BookSectionViewHolder, position: Int) =
                holder.bind(listSection[position])

            override fun getItemCount(): Int = listSection.size
        }
    }

    private inner class BookSectionViewHolder(
        parent: ViewGroup,
        val binding: PerpusItemBinding = PerpusItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        private val adapter by lazy {
            object : ListAdapter<BookTable, BookViewholder>(bookDiffUtil) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewholder =
                    BookViewholder(parent)

                override fun onBindViewHolder(holder: BookViewholder, position: Int) =
                    holder.bind(getItem(position))
            }
        }

        init {
            binding.rvBook.apply {
                adapter = this@BookSectionViewHolder.adapter
                addItemDecoration(bookItemDecor)
                setRecycledViewPool(rvPool)
            }
        }

        fun bind(item: BookSection) {
            binding.item = item
            binding.executePendingBindings()
            adapter.submitList(item.books)
        }
    }

    private inner class BookViewholder(
        parent: ViewGroup,
        val binding: BookItemBinding = BookItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.image.clipToOutline = true
        }

        fun bind(item: BookTable) {
            binding.item = item
            binding.executePendingBindings()
            if (item.cover.isNotEmpty()) {
                Glide.with(binding.image.context)
                    .load(thumbnail(item.cover, binding.image.width))
                    .placeholder(R.drawable.img_loading_book_cover)
                    .error(R.drawable.img_error_book_cover)
                    .thumbnail(0.1f)
                    .fitCenter()
                    .into(binding.image)
            } else {
                binding.image.setImageResource(R.drawable.img_default_book_cover)
            }
            binding.root.setOnClickListener {
                startActivity(
                    Intent(this@PerpusPage, BookDetailPage::class.java).putExtra(
                        "id", item.id
                    )
                )
            }
        }
    }

    fun thumbnail(url: String, width: Int) = url.let {
        var newUrl = it
        newUrl = newUrl.replace("assets.onklas.id/", "thumbnail.onklas.id/")
        newUrl = newUrl.plus(if (!newUrl.contains('?')) "?" else "&")
        newUrl = newUrl.plus("width=${width}")
        Timber.e("load thumbnail: $newUrl")
        newUrl
    }

    private val bookDiffUtil by lazy {
        object : DiffUtil.ItemCallback<BookTable>() {
            override fun areItemsTheSame(oldItem: BookTable, newItem: BookTable): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: BookTable, newItem: BookTable): Boolean =
                oldItem == newItem
        }
    }
}