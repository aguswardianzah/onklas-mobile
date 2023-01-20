package id.onklas.app.pages.ujian

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import id.onklas.app.R
import id.onklas.app.databinding.NilaiUjianPageBinding
import id.onklas.app.databinding.UjianScoredItemBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import timber.log.Timber

class NilaiujianPage : Fragment() {

    private lateinit var binding: NilaiUjianPageBinding
    private val viewmodel by activityViewModels<UjianViewModel> { component.ujianVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = NilaiUjianPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvUjian.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._8sdp),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvUjian.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { refresh() }

        viewmodel.loadingNilai.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )

        lifecycleScope.launchWhenCreated {
            viewmodel.persistDB.ujian().listUjianScored().debounce(100).collect {
                try {
                    Timber.e("data updated --> invalidate")
                    invalidateData()

                    viewmodel.listUjianScoredStudent2.observe(viewLifecycleOwner, {
                        Timber.e("list scored: $it")
                        adapter.submitList(it) {
                            if (!firstLoad) {
                                val layoutManager =
                                    (binding.rvUjian.layoutManager as LinearLayoutManager)
                                val position =
                                    layoutManager.findFirstCompletelyVisibleItemPosition()
                                if (position != RecyclerView.NO_POSITION) {
                                    binding.rvUjian.scrollToPosition(position)
                                }
                            } else {
                                binding.rvUjian.scrollToPosition(0)
                                firstLoad = false
                            }
                        }
                        viewmodel.loadingNilai.postValue(false)
                    })
                } catch (e: Exception) {
                }
            }
//            viewmodel.fetchScored()
        }
    }

    private var firstLoad = true
    private fun refresh() {
        lifecycleScope.launch {
            firstLoad = true
            viewmodel.loadingNilai.postValue(true)
            viewmodel.hasNextScored = true
            viewmodel.scoredStart = -1
            viewmodel.fetchScored()
            invalidateData()
        }
    }

    private fun invalidateData() {
        viewmodel.scoredPagingKey = 0
        viewmodel.scoredDataSource.value?.invalidate()
    }

    private val adapter by lazy {
        object : PagingAdapter<ExamTable, Viewholder>(
            object : DiffUtil.ItemCallback<ExamTable>() {
                override fun areItemsTheSame(oldItem: ExamTable, newItem: ExamTable): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: ExamTable, newItem: ExamTable): Boolean =
                    oldItem == newItem
            }
        ) {
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
        val binding: UjianScoredItemBinding = UjianScoredItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExamTable) {
            binding.item = item
            binding.loading =
                if (item.status == 4)
                    CircularProgressDrawable(binding.root.context).apply {
                        colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        start()
                    }
                else null

            binding.executePendingBindings()

            binding.btnSend.setOnClickListener {
                lifecycleScope.launch {
                    if (viewmodel.answerExam(item.id.toString(), item.subject)) {
                        viewmodel.persistDB.ujian().sendUjian(item.id)
                        viewmodel.scoredPagingKey = 0
                        viewmodel.scoredDataSource.value?.invalidate()
                    }
                }
            }

            binding.root.setOnClickListener { openDetail(item.id) }
            binding.image.setOnClickListener { openDetail(item.id) }
            binding.mapel.setOnClickListener { openDetail(item.id) }
            binding.teacher.setOnClickListener { openDetail(item.id) }
            binding.timePlot.setOnClickListener { openDetail(item.id) }
            binding.attend.setOnClickListener { openDetail(item.id) }
            binding.labelInfo.setOnClickListener { openDetail(item.id) }
            binding.score.setOnClickListener { openDetail(item.id) }
        }

        private fun openDetail(id: Int) {
            startActivity(
                Intent(requireActivity(), UjianDetailPage::class.java)
                    .putExtra(
                        "id", id.toString()
                    )
            )
        }
    }
}