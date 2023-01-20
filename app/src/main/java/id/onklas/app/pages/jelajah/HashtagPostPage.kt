package id.onklas.app.pages.jelajah

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.CoordinatorRvPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.sekolah.MyImageZoom
import id.onklas.app.pages.sekolah.PostAdapterCallback
import id.onklas.app.pages.sekolah.SosmedViewModel
import id.onklas.app.pages.sekolah.adapter.PostAdapter2
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HashtagPostPage : Privatepage() {

    companion object {
        fun open(activity: Activity, hashtag: String) {
            activity.startActivity(
                Intent(activity, HashtagPostPage::class.java).putExtra(
                    "hashtag",
                    hashtag
                )
            )
        }
    }

    private val viewmodel by viewModels<JelajahViewModel> { component.jelajahVmFactory }
    private val sosmedVm by viewModels<SosmedViewModel> { component.sosmedVmFactory }
    private val binding: CoordinatorRvPageBinding by lazy {
        CoordinatorRvPageBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("hashtag") && !intent.getStringExtra("hashtag").isNullOrEmpty()) {
            val hashtag = intent.getStringExtra("hashtag").orEmpty()
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)

                binding.toolbar.setNavigationOnClickListener { finish() }
                title = hashtag
                binding.toolbar.title = hashtag
            }

            binding.swipeRefresh.setOnRefreshListener {
                lifecycleScope.launchWhenCreated {
                    viewmodel.hasMorePost = true
                    viewmodel.isRequestPost = false
                    viewmodel.fetchPopular(0, hashtag)
                    viewmodel.loadingPopular.postValue(true)
                }
            }

            binding.recyclerView.addItemDecoration(
                LinearSpaceDecoration(
                    space = resources.getDimensionPixelSize(R.dimen._8sdp),
                    includeTop = true,
                    includeBottom = true
                )
            )
            binding.recyclerView.adapter = this@HashtagPostPage.adapter
            binding.recyclerView.doOnLayout { viewmodel.rvWidth = it.width }

            viewmodel.loadingPopular.observe(this, Observer(binding.swipeRefresh::setRefreshing))
            viewmodel.listPopular(hashtag).observe(this, {
                adapter.submitList(it) {
                    binding.swipeRefresh.isRefreshing = false
                }
            })
        }
    }

    private val adapter by lazy {
        PostAdapter2(
            object : PostAdapterCallback.PostCallback(this) {
                override fun onClickLike(item: FeedTimeline, liked: Boolean) {
                    GlobalScope.launch {
                        sosmedVm.likePost(item, liked)
                    }
                }

                override fun onDeleteFeed(item: FeedTimeline) {
                    lifecycleScope.launch {
                        val progress =
                            ProgressDialog.show(
                                this@HashtagPostPage,
                                "",
                                "menghapus post"
                            )
                        sosmedVm.deleteFeed(item.feed.feed_id)
                        progress.dismiss()
                    }
                }
            },
            GlideApp.with(this),
            viewmodel.stringUtil,
            viewmodel.pref.getInt("user_id")
        )
    }

    private val zoomHelper: MyImageZoom by lazy { MyImageZoom(this, binding.root) }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return zoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)
    }
}