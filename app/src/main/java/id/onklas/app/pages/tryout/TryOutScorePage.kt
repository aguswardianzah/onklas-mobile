package id.onklas.app.pages.tryout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.TryOutScorePageBinding
import id.onklas.app.databinding.TryOutScoredItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.akm.AkmScoreDetailPage
import id.onklas.app.pages.akm.AkmTable
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class TryOutScorePage : Fragment() {

    private lateinit var binding: TryOutScorePageBinding
    private val viewmodel by activityViewModels<TryOutViewModel> { component.tryoutVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = TryOutScorePageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        binding.rvAkm.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                customEdge = resources.getDimensionPixelSize(R.dimen._16sdp),
                includeTop = true,
                includeBottom = true
            )
        )

        binding.rvAkm.adapter = adapter

//        if (viewmodel.isSchoolScope) {
        viewmodel.listtryoutScore.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewmodel.isLoadingTryoutScored = false
                viewmodel.hasMoreTryoutScored = true
                viewmodel.loadTryoutScored()
            }
        }
//        } else {
//            viewmodel.listScoreAkm.observe(viewLifecycleOwner) {
//                adapter.submitList(it)
//            }
//
//            binding.swipeRefresh.setOnRefreshListener {
//                lifecycleScope.launch {
//                    viewmodel.isLoadingScored = false
//                    viewmodel.hasMoreScored = true
//                    viewmodel.loadAkmScored()
//                }
//            }
//        }


        viewmodel.initLoadingScored.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )
        viewmodel.initLoadingScored.observe(viewLifecycleOwner, Observer {
            Timber.e("loading list $it")
        })

        binding.executePendingBindings()
    }

    private val adapter by lazy {
        object : PagingAdapter<AkmTable, Viewholder>(object : DiffUtil.ItemCallback<AkmTable>() {
            override fun areItemsTheSame(oldTable: AkmTable, newTable: AkmTable): Boolean =
                oldTable.id == newTable.id

            override fun areContentsTheSame(oldTable: AkmTable, newTable: AkmTable): Boolean =
                oldTable == newTable
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }

    @SuppressLint("SetTextI18n")
    private inner class Viewholder(
        parent: ViewGroup,
        val binding: TryOutScoredItemBinding = TryOutScoredItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AkmTable) {
            binding.item = item
            binding.btnAttend.setOnClickListener { select(item) }
            binding.background = if (item.exam_template == "AKM") ContextCompat.getDrawable(
                requireContext(),
                R.drawable.tag_dark_blue
            ) else ContextCompat.getDrawable(requireContext(), R.drawable.tag_purple)
//            binding.mapel.setOnClickListener { select(item) }
//            binding.root.setOnClickListener { select(item) }
            binding.executePendingBindings()
        }

        private fun select(item: AkmTable) {
            startActivityForResult(
                Intent(
                    requireActivity(),
                    AkmScoreDetailPage::class.java
                )
                    .putExtra("id", item.id)
                    .putExtra("isTryout", true)
                    .putExtra("isSchoolScope", viewmodel.isSchoolScope), 219
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 219 && resultCode == Activity.RESULT_OK) {
            binding.swipeRefresh.isRefreshing = true
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}