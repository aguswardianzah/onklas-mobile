package id.onklas.app.pages.comment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.otaliastudios.autocomplete.*
import id.onklas.app.R
import id.onklas.app.databinding.CommentItemBinding
import id.onklas.app.databinding.CommentPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.akun.ProfilePage
import id.onklas.app.pages.jelajah.HashtagPostPage
import id.onklas.app.pages.listlike.ListLikePage
import id.onklas.app.pages.sekolah.sosmed.FeedCommentWithUser
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import id.onklas.app.utils.UsernameAdapter
import id.onklas.app.utils.hideKeyboard
import kotlinx.coroutines.launch
import timber.log.Timber

class CommentPage : Privatepage() {

    companion object {
        fun open(activity: Activity, feedId: Int) {
            activity.startActivity(
                Intent(activity, CommentPage::class.java).putExtra(
                    "feed_id",
                    feedId
                )
            )
        }
    }

    private val binding by lazy { CommentPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<CommentViewModel> { component.commentVmFactory }
    private var feedId: Int = 0
    private val usernameAdapter by lazy { UsernameAdapter() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.viewmodel = viewmodel
        viewmodel.errorString.observe(this, Observer(this::toast))
        binding.info.movementMethod = LinkMovementMethod.getInstance()

        binding.rvComments.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._12sdp)
            )
        )
        binding.rvComments.adapter = commentAdapter
        binding.rvComments.isNestedScrollingEnabled = false

        binding.imgReply.isEnabled = false
        binding.inputReply.doAfterTextChanged {
            if (binding.inputReply.text.toString().isNotEmpty()) {
                binding.replyLayout.setBackgroundResource(R.drawable.border_reply_2_primary)
                binding.imgReply.isEnabled = true
            } else {
                binding.replyLayout.setBackgroundResource(R.drawable.border_reply_2)
                binding.imgReply.isEnabled = false
            }
        }

        viewmodel.listUser.observe(this, Observer(usernameAdapter::submitList))

        Autocomplete.on<UserTable>(binding.inputReply)
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

        binding.imgReply.setOnClickListener {
            lifecycleScope.launch {
                loading(msg = "mengirim balasan")
                val commentResp = viewmodel.sendCommend()
                dismissloading()
                if (commentResp) {
                    binding.inputReply.setText("")
                    hideKeyboard(binding.inputReply)
                    viewmodel.comments(viewmodel.feedId.value!!)
                        .observe(this@CommentPage, Observer(commentAdapter::submitList))
                    init()
                } else {
                    toast("gagal mengirim balasan, mohon ulangi beberapa saat lagi")
                }
            }
        }

        viewmodel.post.observe(this, {
            it.files.firstOrNull()?.let {
                if (it.type.contains("image", true)) {
                    binding.imageUrl = it.path
                    binding.image.visibility = View.VISIBLE
                    binding.image.setOnClickListener { v ->
                        startActivity(
                            Intent(
                                this@CommentPage,
                                ImageViewPage::class.java
                            ).setData(Uri.parse(it.path)),
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                this@CommentPage,
                                binding.image,
                                "image"
                            ).toBundle()
                        )
                    }
                } else
                    binding.image.visibility = View.GONE
            } ?: run { binding.image.visibility = View.GONE }

            binding.content.text = viewmodel.stringUtil.buildUserContentComment(
                this@CommentPage,
                it.userTable.name,
                it.feed.feed_body,
                {
                    ProfilePage.open(
                        this@CommentPage,
                        if (it.userTable.nisn_nik.isNotEmpty()) it.userTable.nisn_nik else it.userTable.nis_nik
                    )
                },
                { hashtag -> HashtagPostPage.open(this@CommentPage, hashtag) },
                { username -> ProfilePage.open(this@CommentPage, username = username) }
            )
            binding.time.text = it.feed.timeString
            binding.info.text =
                SpannableStringBuilder("${it.feed.count_likes} Suka ").apply {
                    setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            ListLikePage.open(this@CommentPage, it.feed.feed_id.toInt())
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                            ds.color =
                                ContextCompat.getColor(this@CommentPage, R.color.gray)
                        }
                    }, 0, length, 0)
                    append("- ${it.feed.count_comments} Komentar")
                }
        })

        viewmodel.feedId.observe(this, {
            lifecycleScope.launchWhenCreated {
                loading(msg = "menampilkan post")
                viewmodel.getPost(it)
                viewmodel.commentStart = -1
                viewmodel.hasMoreComment = true
                viewmodel.fetchComments()
                viewmodel.comments(it).observe(this@CommentPage, {
                    commentAdapter.submitList(it)
                })
                viewmodel.listUser.postValue(viewmodel.db.feed().searchUsername())
                dismissloading()
            }
        })

        if (intent.hasExtra("feed_id") && intent.getIntExtra("feed_id", 0) > 0) {
            feedId = intent.getIntExtra("feed_id", 0)
            Timber.e("intent feed_id: ${intent.getIntExtra("feed_id", 0)}")
            viewmodel.feedId.postValue(feedId)
            NotificationManagerCompat.from(applicationContext).cancel(feedId)
        } else {
            close()
            return
        }
    }

    private fun init() {
        lifecycleScope.launch {
            viewmodel.feedId.postValue(feedId)
        }
    }

    private fun close() {
        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            .setTitle("Post Tidak tersedia")
            .setMessage(
                viewmodel.errorString.value ?: "Halaman yang kamu cari sudah tidak tersedia"
            )
            .setCancelable(false)
            .setPositiveButton("Tutup") { dialog, which ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private val commentAdapter by lazy {
        object : PagingAdapter<FeedCommentWithUser, Viewholder>(object :
            DiffUtil.ItemCallback<FeedCommentWithUser>() {
            override fun areItemsTheSame(
                oldItem: FeedCommentWithUser,
                newItem: FeedCommentWithUser
            ): Boolean = oldItem.comment.cid == newItem.comment.cid

            override fun areContentsTheSame(
                oldItem: FeedCommentWithUser,
                newItem: FeedCommentWithUser
            ): Boolean = oldItem == newItem
        }) {
            override fun createItemViewholder(
                parent: ViewGroup,
                viewType: Int
            ): Viewholder = Viewholder(parent)

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int
            ) = getItem(position)?.let { it1 -> holder.bind(it1) } ?: Unit

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) = getItem(position)?.let { it1 -> holder.bind(it1) } ?: Unit
        }
    }

    inner class Viewholder(
        parent: ViewGroup,
        val binding: CommentItemBinding = CommentItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeedCommentWithUser) {
            binding.item = item
            binding.image.setOnClickListener {
                item.userTable?.let {
                    val nis = if (it.nisn_nik.isNotEmpty()) it.nisn_nik else it.nis_nik
                    ProfilePage.open(this@CommentPage, nisn_nik = nis)
                }
            }
            binding.content.text =
                viewmodel.stringUtil.buildUserContentComment(
                    binding.content.context,
                    item.userTable?.name ?: "User",
                    item.comment.comment,
                    onClickUser = {
                        item.userTable?.let {
                            val nis = if (it.nisn_nik.isNotEmpty()) it.nisn_nik else it.nis_nik
                            ProfilePage.open(this@CommentPage, nisn_nik = nis)
                        }
                    },
                    onClickMention = { username ->
                        ProfilePage.open(this@CommentPage, username = username)
                    },
                    onClickHashtag = { hashtag -> HashtagPostPage.open(this@CommentPage, hashtag) }
                )
            binding.content.movementMethod = LinkMovementMethod.getInstance()
            binding.option.setOnClickListener {
                MaterialAlertDialogBuilder(this@CommentPage)
                    .setItems(
                        arrayOf(
                            "Buka profil ${item.userTable?.name}"
//                            "Laporkan konten",
//                            "Lainnya"
                        )
                    ) { dialog, which ->
                        item.userTable?.let {
                            val nis = if (it.nisn_nik.isNotEmpty()) it.nisn_nik else it.nis_nik
                            ProfilePage.open(this@CommentPage, nisn_nik = nis)
                        }
                        dialog.dismiss()
                    }
                    .show()
            }
            binding.executePendingBindings()
        }
    }
}