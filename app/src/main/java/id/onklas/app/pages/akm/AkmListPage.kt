package id.onklas.app.pages.akm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.AkmItemBinding
import id.onklas.app.databinding.AkmListPageBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class AkmListPage : Fragment() {

    private lateinit var binding: AkmListPageBinding
    private val viewmodel by activityViewModels<AkmViewModel> { component.akmVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = AkmListPageBinding.inflate(inflater, container, false).also { binding = it }.root

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

        if (viewmodel.isSchoolScope) {
            viewmodel.calUjian.observe(viewLifecycleOwner) {
                viewmodel.listUjianSchool().observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                }
            }

            binding.swipeRefresh.setOnRefreshListener {
                viewmodel.hasMoreUjianSchool = true
                viewmodel.loadUjianSchool()
            }
        } else {
            viewmodel.listAkm.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

            binding.swipeRefresh.setOnRefreshListener {
                viewmodel.hasMoreList = true
                viewmodel.loadAkm()
            }
        }

        viewmodel.initLoadingList.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )

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

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: AkmItemBinding = AkmItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AkmTable) {
            binding.item = item
            binding.btnAttend.text = if (viewmodel.isSchoolScope) "Ikuti Ujian" else "Ikuti AKM"
            binding.btnAttend.setOnClickListener { select(item) }
            binding.mapel.setOnClickListener { select(item) }
            binding.root.setOnClickListener { select(item) }
            binding.executePendingBindings()
        }

        private fun select(item: AkmTable) {
            startActivityForResult(
                Intent(
                    requireActivity(),
                    AkmDetailPage::class.java
                )
                    .putExtra("id", item.id)
                    .putExtra("isSchoolScope", viewmodel.isSchoolScope), 219
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 219 && resultCode == Activity.RESULT_OK) {
            if (data?.hasExtra("showScore") == true && data.getBooleanExtra("showScore", false)) {
                findNavController().navigate(R.id.action_global_akmScorePage)
                (requireActivity() as AkmPage).openScoreList()
            } else
                binding.swipeRefresh.isRefreshing = true
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}