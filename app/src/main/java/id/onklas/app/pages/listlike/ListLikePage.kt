package id.onklas.app.pages.listlike

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.ListLikeItemBinding
import id.onklas.app.databinding.ListLikePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.akun.ProfilePage
import id.onklas.app.pages.sekolah.sosmed.FeedListLike
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class ListLikePage : Privatepage() {

    companion object {
        fun open(activity: Activity, feedId: Int) {
            activity.startActivity(
                Intent(activity, ListLikePage::class.java)
                    .putExtra("feed_id", feedId)
            )
        }
    }

    private val binding by lazy { ListLikePageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ListLikeViewModel> { component.listLikeVmFactory }
    private var feedId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        if (intent.hasExtra("feed_id") && intent.getIntExtra("feed_id", 0) > 0) {
            feedId = intent.getIntExtra("feed_id", 0)
            lifecycleScope.launch {
                loading(msg = "mencari daftar menyukai post")
                viewmodel.fetchLike(feedId)
                binding.rvLikes.adapter = adapter
                binding.rvLikes.addItemDecoration(
                    LinearSpaceDecoration(
                        space = resources.getDimensionPixelSize(R.dimen._8sdp)
                    )
                )

                viewmodel.getListLike(feedId).observe(this@ListLikePage, {
                    dismissloading()
                    adapter.submitList(it)
                })
            }
        } else {
            close()
        }
    }

    private fun close() {
        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle("Post Tidak tersedia")
            .setMessage("Halaman yang kamu cari sudah tidak tersedia")
            .setCancelable(false)
            .setPositiveButton("Tutup") { dialog, which ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private val adapter by lazy {
        object :
            PagingAdapter<FeedListLike, Viewholder>(object : DiffUtil.ItemCallback<FeedListLike>() {
                override fun areItemsTheSame(
                    oldItem: FeedListLike,
                    newItem: FeedListLike
                ): Boolean =
                    oldItem.userTable?.uuid == newItem.userTable?.uuid

                override fun areContentsTheSame(
                    oldItem: FeedListLike,
                    newItem: FeedListLike
                ): Boolean =
                    oldItem == newItem
            }) {

            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.userTable?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.userTable?.let { holder.bind(it) }
            }
        }
    }

    inner class Viewholder(
        parent: ViewGroup,
        val binding: ListLikeItemBinding = ListLikeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserTable) {
            binding.item = item
            binding.executePendingBindings()
            binding.root.setOnClickListener { ProfilePage.open(this@ListLikePage, item.nisn_nik) }
            binding.image.setOnClickListener { ProfilePage.open(this@ListLikePage, item.nisn_nik) }
            binding.name.setOnClickListener { ProfilePage.open(this@ListLikePage, item.nisn_nik) }
        }
    }
}