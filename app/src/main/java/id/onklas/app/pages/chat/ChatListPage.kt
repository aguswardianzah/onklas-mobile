package id.onklas.app.pages.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.ChatListPageBinding
import id.onklas.app.databinding.ConversationItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter

class ChatListPage : Privatepage() {

    private val binding by lazy { ChatListPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ChatViewModel> { component.chatVmFactory }
    private val type by lazy { intent.getStringExtra("type").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.rvConv.addItemDecoration(
            LinearSpaceDecoration(space = 1)
        )
        binding.rvConv.adapter = convAdapter

        viewModel.closeAllChat()
        viewModel.conversation(type).observe(this) {
            it.forEach {
                NotificationManagerCompat.from(applicationContext)
                    .cancel(viewModel.stringUtil.numOnly(it.conversation.with))
            }

            convAdapter.submitList(it)

            viewModel.emptyConversation.postValue(it.isEmpty())
        }
        viewModel.fetchContact()

        binding.fabNew.visibility = if (type == "kwu") View.GONE else View.VISIBLE

        binding.fabNew.setOnClickListener {
            startActivity(Intent(this, SelectContactChatPage::class.java))
        }

        binding.executePendingBindings()
    }

    private val convAdapter by lazy {
        object : PagingAdapter<ConversationWithLastMessage, ConversationVH>(object :
            DiffUtil.ItemCallback<ConversationWithLastMessage>() {
            override fun areItemsTheSame(
                oldItem: ConversationWithLastMessage,
                newItem: ConversationWithLastMessage
            ): Boolean = oldItem.conversation.with == newItem.conversation.with

            override fun areContentsTheSame(
                oldItem: ConversationWithLastMessage,
                newItem: ConversationWithLastMessage
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: ConversationWithLastMessage,
                newItem: ConversationWithLastMessage
            ): Any = newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ConversationVH =
                ConversationVH(parent)

            override fun bindItemViewholder(holder: ConversationVH, position: Int) {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }

            override fun bindItemViewholder(
                holder: ConversationVH,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty())
                    holder.update(payloads.first() as ConversationWithLastMessage)
                else
                    bindItemViewholder(holder, position)
            }
        }
    }

    private inner class ConversationVH(
        parent: ViewGroup,
        val binding: ConversationItemBinding = ConversationItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ConversationWithLastMessage) {
            binding.conversation = item.conversation
            binding.chat = item.chat
            binding.myId = viewModel.myId
            binding.stringUtil = viewModel.stringUtil

            binding.status = item.chat?.status
            binding.unread = item.conversation.unread
            binding.message = item.chat?.message
            binding.date = item.chat?.date

            binding.root.setOnClickListener {
                startActivity(
                    Intent(this@ChatListPage, ChatPage::class.java)
                        .putExtra("with", item.conversation.with)
                        .putExtra("name", item.conversation.name)
                        .putExtra("image", item.conversation.img_profile)
                )
            }

            binding.executePendingBindings()
        }

        fun update(item: ConversationWithLastMessage) {
            binding.chat = item.chat
            binding.status = item.chat?.status
            binding.unread = item.conversation.unread
            binding.message = item.chat?.message
            binding.date = item.chat?.date

            binding.executePendingBindings()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenCreated {
            viewModel.db.chat().closeConversation()
        }
    }
}