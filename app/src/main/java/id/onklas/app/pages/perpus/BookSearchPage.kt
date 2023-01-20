package id.onklas.app.pages.perpus

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.onklas.app.R
import id.onklas.app.databinding.BookSearchItemBinding
import id.onklas.app.databinding.BookSearchPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch
import timber.log.Timber

class BookSearchPage : Privatepage() {

    private val binding by lazy { BookSearchPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PerpusViewModel> { component.perpusVmFactory }
    private val runSearch by lazy { Runnable { search() } }
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewmodel.loadingSearchBook.observe(this, Observer(binding.swipeRefresh::setRefreshing))

        binding.swipeRefresh.setOnRefreshListener { search() }
        binding.searchLayout.inputSearch.doAfterTextChanged {
            handler.removeCallbacks(runSearch)
            handler.postDelayed(runSearch, 500)
        }

        binding.rvBook.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeBottom = true
            )
        )
        binding.rvBook.adapter = adapter

        viewmodel.listSearchBook.observe(this, {
            adapter.submitList(it)
            binding.rvBook.scrollToPosition(0)
        })

        search()
    }

    private fun search() {
        lifecycleScope.launch {
            viewmodel.searchBook(binding.searchLayout.inputSearch.text.toString())
        }
    }

    private val adapter by lazy {
        object :
            ListAdapter<BookTable, BookViewholder>(object : DiffUtil.ItemCallback<BookTable>() {
                override fun areItemsTheSame(oldItem: BookTable, newItem: BookTable): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: BookTable, newItem: BookTable): Boolean =
                    oldItem == newItem
            }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewholder =
                BookViewholder(parent)

            override fun onBindViewHolder(holder: BookViewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class BookViewholder(
        parent: ViewGroup,
        val binding: BookSearchItemBinding = BookSearchItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

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
                    Intent(this@BookSearchPage, BookDetailPage::class.java).putExtra(
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
}