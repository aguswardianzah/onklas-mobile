package id.onklas.app.pages.tryout

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.TryOutItemBinding
import id.onklas.app.databinding.TryOutSchedulePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.akm.AkmDetailPage
import id.onklas.app.pages.akm.AkmTable
import id.onklas.app.pages.akm.ExamType
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import timber.log.Timber

class TryOutSchedulePage : Fragment() {

    private lateinit var binding: TryOutSchedulePageBinding
    private val viewmodel by activityViewModels<TryOutViewModel> { component.tryoutVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        TryOutSchedulePageBinding.inflate(inflater, container, false).also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
//            viewmodel.loadTryout().observe(viewLifecycleOwner) {
        viewmodel.listTryout().observe(viewLifecycleOwner) {
            Timber.e("list tryout $it")
            adapter.submitList(it)
        }
//            }

        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.hasMoreTryout = true
            viewmodel.loadTryout()
        }
//        }

        viewmodel.initLoadingList.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )
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
        val binding: TryOutItemBinding = TryOutItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AkmTable) {
            binding.item = item
            binding.background = if (item.exam_template == "AKM") ContextCompat.getDrawable(
                requireContext(),
                R.drawable.tag_dark_blue
            ) else ContextCompat.getDrawable(requireContext(), R.drawable.tag_purple)
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
                    .putExtra("examType", ExamType.TRYOUT)
                    .putExtra("isSchoolScope", viewmodel.isSchoolScope), 219
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 219 && resultCode == Activity.RESULT_OK) {
            if (data?.hasExtra("showScore") == true && data.getBooleanExtra("showScore", false)) {
                findNavController().navigate(R.id.action_global_tryoutscorepage)
                (requireActivity() as? TryOutPage)?.openScoreList()
            } else
                binding.swipeRefresh.isRefreshing = true
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}