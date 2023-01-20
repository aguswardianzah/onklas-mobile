package id.onklas.app.pages.chat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.ChatContactItemBinding
import id.onklas.app.databinding.SelectContactChatPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter

class SelectContactChatPage : Privatepage() {

    private val binding by lazy { SelectContactChatPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ChatViewModel> { component.chatVmFactory }
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.rvKontak.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvKontak.adapter = contactAdapter

        binding.inSearch.doAfterTextChanged {
            handler.removeCallbacks(runSearch)
            handler.postDelayed(runSearch, 250)
        }

        viewModel.fetchContact()

        initData()
    }

    private val runSearch by lazy { Runnable { initData(binding.inSearch.text.toString()) } }

    private var currLiveData: LiveData<PagedList<UserTable>>? = null
    private fun initData(search: String = "") {
        firstLoad = true
        binding.progress.visibility = View.VISIBLE
        binding.progressLabel.visibility = View.VISIBLE
        currLiveData?.removeObservers(this)
        currLiveData = viewModel.contactList(search).apply {
            observe(this@SelectContactChatPage, listObserver)
        }
    }

    private var firstLoad = true
    private val listObserver by lazy {
        Observer<PagedList<UserTable>> {
            contactAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvKontak.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvKontak.scrollToPosition(position)
                    }
                } else {
                    binding.rvKontak.scrollToPosition(0)
                    firstLoad = false
                }
                binding.progress.visibility = View.GONE
                binding.progressLabel.visibility = View.GONE
            }
        }
    }

    private val contactAdapter by lazy {
        object : PagingAdapter<UserTable, ContactVH>(
            object : DiffUtil.ItemCallback<UserTable>() {
                override fun areItemsTheSame(oldItem: UserTable, newItem: UserTable): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: UserTable, newItem: UserTable): Boolean =
                    oldItem == newItem
            }
        ) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ContactVH =
                ContactVH(parent)

            override fun bindItemViewholder(holder: ContactVH, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: ContactVH,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }

    private inner class ContactVH(
        parent: ViewGroup,
        val binding: ChatContactItemBinding = ChatContactItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserTable) {
            binding.item = item
            binding.root.setOnClickListener {
                startActivity(
                    Intent(this@SelectContactChatPage, ChatPage::class.java)
                        .putExtra("with", item.wallet_id)
                        .putExtra("name", item.name)
                        .putExtra("image", item.user_avatar_image)
                )
                finish()
            }
            binding.executePendingBindings()
        }
    }
}