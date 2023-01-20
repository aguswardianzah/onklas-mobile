package id.onklas.app.pages.ppob.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.GameItemBinding
import id.onklas.app.databinding.SelectGamePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.utils.GridSpacingItemDecoration
import kotlinx.coroutines.launch

class SelectGamePage : BaseFragment() {

    private lateinit var binding: SelectGamePageBinding
    private val viewmodel by activityViewModels<GameViewModel> { component.gameVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SelectGamePageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvGame.addItemDecoration(
            GridSpacingItemDecoration(
                3,
                resources.getDimensionPixelSize(R.dimen._12sdp),
                true,
                resources.getDimensionPixelSize(R.dimen._16sdp),
            )
        )
        binding.rvGame.adapter = adapter

        viewmodel.loadingGame.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )
        viewmodel.listProvider.observe(viewLifecycleOwner, Observer(adapter::submitList))

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch { viewmodel.loadGame() }
        }
    }

    private val adapter by lazy {
        object : ListAdapter<Pair<Int, String>, Viewholder>(object :
            DiffUtil.ItemCallback<Pair<Int, String>>() {
            override fun areItemsTheSame(
                oldItem: Pair<Int, String>,
                newItem: Pair<Int, String>
            ): Boolean = oldItem.second == newItem.second

            override fun areContentsTheSame(
                oldItem: Pair<Int, String>,
                newItem: Pair<Int, String>
            ): Boolean = oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private inner class Viewholder(
        parent: ViewGroup, val binding: GameItemBinding = GameItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<Int, String>) {
            binding.image.setImageResource(item.first)
            binding.name.text = item.second
            binding.image.setOnClickListener { select(item.second) }
            binding.name.setOnClickListener { select(item.second) }
            binding.root.setOnClickListener { select(item.second) }
        }

        private fun select(name: String) {
            viewmodel.selectProvider(name)
            findNavController().navigate(R.id.action_selectGamePage_to_selectVoucherPage)
        }
    }
}