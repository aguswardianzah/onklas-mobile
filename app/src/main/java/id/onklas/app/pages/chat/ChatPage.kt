package id.onklas.app.pages.chat

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vanniktech.emoji.EmojiPopup
import id.onklas.app.R
import id.onklas.app.databinding.*
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import id.onklas.app.utils.showKeyboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatPage : Privatepage() {

    private val binding by lazy { ChatPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ChatViewModel> { component.chatVmFactory }
    private val with by lazy { intent.getStringExtra("with").orEmpty() }
    private val name by lazy { intent.getStringExtra("name") ?: "Nama Pengguna" }
    private val image by lazy { intent.getStringExtra("image").orEmpty() }

    private val productId by lazy { intent.getIntExtra("payload_productId", 0) }
    private val productName by lazy { intent.getStringExtra("payload_productName").orEmpty() }
    private val productImage by lazy { intent.getStringExtra("payload_productImage").orEmpty() }
    private val productPrice by lazy { intent.getIntExtra("payload_productPrice", 0) }

    private lateinit var layoutManager: LinearLayoutManager
    private var firstLoad = true

    private val emojiPopup by lazy {
        EmojiPopup.Builder.fromRootView(binding.root).build(binding.inputChat)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (with.isEmpty()) {
            alert(msg = "Chat tidak valid", okClick = { finish() })
            return
        }

        window.setBackgroundDrawableResource(R.drawable.img_chat_bg)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }
        window.statusBarColor = Color.WHITE

        NotificationManagerCompat.from(applicationContext)
            .cancel(viewModel.stringUtil.numOnly(with))

        viewModel.with = with
        viewModel.name = name

        binding.lifecycleOwner = this

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.rvChat.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._2sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvChat.adapter = chatAdapter
        layoutManager = binding.rvChat.layoutManager as LinearLayoutManager

        binding.icEmoji.setOnClickListener {
            if (emojiPopup.isShowing) {
                binding.icEmoji.setImageResource(R.drawable.emoji_google_category_smileysandpeople)
                showKeyboard()
            } else {
                binding.icEmoji.setImageResource(R.drawable.ic_keyboard)
            }

            emojiPopup.toggle()
        }

        binding.inputChat.doAfterTextChanged {
            binding.btnSend.isEnabled = it.toString().isNotBlank()
        }

        binding.btnSend.setOnClickListener {
            firstLoad = true
            viewModel.sendChat(binding.inputChat.text.toString())
            binding.inputChat.setText("")
        }

        // register listener keyboard visibility
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            val screenHeight = binding.root.rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    binding.rvChat.smoothScrollToPosition(0)
                }
            } else {
                // keyboard is closed
            }
        }

        // listen input layout to set padding of recyclerview
        binding.inputLayout.viewTreeObserver.addOnDrawListener {
            binding.rvChat.setPadding(
                binding.rvChat.paddingLeft,
                binding.rvChat.paddingTop,
                binding.rvChat.paddingRight,
                binding.inputLayout.height + resources.getDimensionPixelSize(R.dimen._8sdp)
            )
        }

        // register broadcast listener for new chat
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        Timber.e("receive broadcast new chat")
                        // scroll to bottom on new chat
                        if (layoutManager.findFirstVisibleItemPosition() == 0) {
                            firstLoad = true
                        }
                    }
                },
                IntentFilter("${packageName}.newChat")
            )

        loading(msg = "memulai obrolan")
        lifecycleScope.launchWhenCreated {
            viewModel.setConversation(with, name, image)
            viewModel.currentConv(with).observe(this@ChatPage) {
                binding.conv = it
                if (it != null)
                    binding.presence = it.presence
                binding.executePendingBindings()

                dismissloading()
            }

            viewModel.chat(with).observe(this@ChatPage) {
//                Timber.e("list chat: ${it.joinToString("\n")}")
                chatAdapter.submitList(it) {
                    if (firstLoad) {
                        firstLoad = false
                        binding.rvChat.smoothScrollToPosition(0)
                    }
                }
            }

            if (productId > 0) {
                viewModel.insertProductChat(productId, productName, productImage, productPrice)
            }
        }

        binding.executePendingBindings()
    }

    override fun onDestroy() {
        GlobalScope.launch {
            viewModel.closeConv()
        }
        super.onDestroy()
    }

    override fun onPause() {
        lifecycleScope.launchWhenCreated {
            viewModel.db.chat().closeConversation()
        }
        super.onPause()
    }

    override fun onResume() {
        lifecycleScope.launchWhenCreated {
            viewModel.setConversation(with, name, image)
        }
        super.onResume()
    }

    private val chatAdapter by lazy {
        object : PagingAdapter<ChatItem, ChatVH>(object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean =
                oldItem == newItem

            override fun getChangePayload(oldItem: ChatItem, newItem: ChatItem): Any = newItem
        }) {

            override fun getItemViewType(position: Int): Int = getItem(position)?.let {
                when (it.type) {
                    ChatType.CHAT_TYPE_INFO -> 2
                    ChatType.CHAT_TYPE_DATE -> 3
                    ChatType.CHAT_TYPE_PRODUCT -> {
                        if (it.from != viewModel.myId) 4
                        else 5
                    }
                    else -> {
                        if (it.from != viewModel.myId) 6
                        else 7
                    }
                }
            } ?: 5

            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ChatVH =
                when (viewType) {
                    2 -> InfoVH(parent)
                    3 -> DateVH(parent)
                    4 -> IncomingProductVH(parent)
                    5 -> OutgoingProductVH(parent)
                    6 -> IncomingVH(parent)
                    else -> OutgoingVH(parent)
                }

            override fun bindItemViewholder(holder: ChatVH, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: ChatVH,
                position: Int,
                payloads: MutableList<Any>
            ) {
                if (payloads.isNotEmpty() && holder is OutgoingVH) {
                    holder.update(payloads.first() as ChatItem)
                } else
                    bindItemViewholder(holder, position)
            }
        }
    }

    private abstract inner class ChatVH(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: ChatItem)
        abstract fun update(item: ChatItem)
    }

    private inner class IncomingVH(
        parent: ViewGroup,
        val binding: ChatIncomingItem2Binding = ChatIncomingItem2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : ChatVH(binding.root) {
        override fun bind(item: ChatItem) {
            binding.item = item
            binding.isFirst = item.first
            binding.timeFormat = viewModel.timeFormat
            binding.executePendingBindings()
        }

        override fun update(item: ChatItem) {
        }
    }

    private inner class OutgoingVH(
        parent: ViewGroup,
        val binding: ChatOutgoingItem2Binding = ChatOutgoingItem2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : ChatVH(binding.root) {
        override fun bind(item: ChatItem) {
            binding.item = item
            binding.isFirst = item.first
            binding.status = item.status
            binding.timeFormat = viewModel.timeFormat
            binding.executePendingBindings()
        }

        override fun update(item: ChatItem) {
            binding.status = item.status
            binding.executePendingBindings()
        }
    }

    private inner class IncomingProductVH(
        parent: ViewGroup,
        val binding: ChatProductItemBinding = ChatProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : ChatVH(binding.root) {
        override fun bind(item: ChatItem) {
            binding.item = item
            binding.isFirst = item.first
            binding.status = item.status
            binding.timeFormat = viewModel.timeFormat
            binding.stringUtil = viewModel.stringUtil
            binding.executePendingBindings()
        }

        override fun update(item: ChatItem) {
            binding.status = item.status
            binding.executePendingBindings()
        }
    }

    private inner class OutgoingProductVH(
        parent: ViewGroup,
        val binding: ChatProductItemBinding = ChatProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : ChatVH(binding.root) {
        override fun bind(item: ChatItem) {
            binding.item = item
            binding.isFirst = item.first
            binding.status = item.status
            binding.timeFormat = viewModel.timeFormat
            binding.stringUtil = viewModel.stringUtil
            binding.executePendingBindings()
        }

        override fun update(item: ChatItem) {
            binding.status = item.status
            binding.executePendingBindings()
        }
    }

    private inner class DateVH(
        parent: ViewGroup,
        val binding: ChatDateBinding = ChatDateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : ChatVH(binding.root) {
        override fun bind(item: ChatItem) {
            binding.item = item
            binding.dateFormat = viewModel.dateFormat
            binding.executePendingBindings()
        }

        override fun update(item: ChatItem) {
        }
    }

    private inner class InfoVH(
        parent: ViewGroup,
        val binding: ChatInfoBinding = ChatInfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : ChatVH(binding.root) {
        override fun bind(item: ChatItem) {
            binding.item = item
            binding.executePendingBindings()
        }

        override fun update(item: ChatItem) {
        }
    }
}