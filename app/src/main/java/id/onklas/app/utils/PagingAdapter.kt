package id.onklas.app.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.LoadMoreLoadingBinding

const val VIEW_TYPE_LOAD_MORE = 0
const val VIEW_TYPE_ITEM = 1

abstract class PagingAdapter<T : Any, V : RecyclerView.ViewHolder>(
    private val diffUtil: DiffUtil.ItemCallback<T>,
    private val retryLoadMore: () -> Unit = {}
) :
    PagedListAdapter<T, V>(diffUtil) {

    private var loadMoreState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V =
        if (viewType == VIEW_TYPE_LOAD_MORE)
            LoadMoreViewholder(parent, retryLoadMore) as V
        else
            createItemViewholder(parent, viewType)

    abstract fun createItemViewholder(parent: ViewGroup, viewType: Int): V

    override fun onBindViewHolder(holder: V, position: Int) {
        (holder as? PagingAdapter<*, *>.LoadMoreViewholder)?.bind(loadMoreState)
            ?: bindItemViewholder(holder, position)
    }

    override fun onBindViewHolder(holder: V, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty())
            bindItemViewholder(holder, position, payloads)
        else
            bindItemViewholder(holder, position)
    }

    abstract fun bindItemViewholder(holder: V, position: Int)
    abstract fun bindItemViewholder(holder: V, position: Int, payloads: MutableList<Any>)

    override fun getItemViewType(position: Int): Int =
        if (hasExtraRow() && position == itemCount - 1)
            VIEW_TYPE_LOAD_MORE else VIEW_TYPE_ITEM

    private fun hasExtraRow() = loadMoreState?.let { it != NetworkState.SUCCESS } ?: false

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.loadMoreState
        val hadExtraRow = hasExtraRow()
        this.loadMoreState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    inner class LoadMoreViewholder(
        val parent: ViewGroup,
        private val retryLoadMore: () -> Unit,
        val binding: LoadMoreLoadingBinding = LoadMoreLoadingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRetry.setOnClickListener { retryLoadMore.invoke() }
        }

        fun bind(networkState: NetworkState?) {
            networkState?.let {
                binding.label.text = it.msg
                when (it.state) {
                    NETWORK_LOADING -> {
                        binding.root.visibility = View.VISIBLE
                        binding.progress.visibility = View.VISIBLE
                        binding.btnRetry.visibility = View.INVISIBLE
                        binding.imgInfo.visibility = View.INVISIBLE
                    }
                    NETWORK_FAILED -> {
                        binding.root.visibility = View.VISIBLE
                        binding.progress.visibility = View.INVISIBLE
                        binding.btnRetry.visibility = View.VISIBLE
                        binding.imgInfo.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.root.visibility = View.GONE
                    }
                }
            }
        }

        fun error() {

        }
    }
}