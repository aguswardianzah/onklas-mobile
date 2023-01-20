package id.onklas.app.pages.pembayaran.spp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.SppPaidItemBinding
import id.onklas.app.databinding.SppPaidPageBinding
import id.onklas.app.databinding.SppTagihanItemBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class SppPaidPage : Fragment() {

    private lateinit var binding: SppPaidPageBinding
    private val viewmodel by activityViewModels<SppViewModel> { component.sppVmFactory }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SppPaidPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.pagePaid = 0
                viewmodel.fetchSpp(1, true)
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.rvSpp.adapter = adapter
        binding.rvSpp.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ), includeTop = true, includeBottom = true
            )
        )

        viewmodel.listPaid.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it
                .onEach {
                    it.paid_at =
                        try {
                            viewmodel.paidDateFormat.format(viewmodel.srcDateFormat.parse(it.paid_at))
                        } catch (e: Exception) {
                            it.paid_at
                        }
                }
            ) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvSpp.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvSpp.scrollToPosition(position)
                    }
                } else {
                    binding.rvSpp.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })

//        lifecycleScope.launch { viewmodel.fetchSpp(1, true) }
    }

    private val adapter by lazy {
        object : PagingAdapter<SppTable, Viewholder>(viewmodel.diffUtil) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    inner class Viewholder(
        parent: ViewGroup,
        val binding: SppPaidItemBinding = SppPaidItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SppTable) {
            binding.item = item
            binding.stringUtil = viewmodel.stringUtil
            binding.student = viewmodel.student
            binding.executePendingBindings()
        }
    }
}