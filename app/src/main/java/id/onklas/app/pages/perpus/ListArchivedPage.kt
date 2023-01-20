package id.onklas.app.pages.perpus

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
import id.onklas.app.databinding.ListRentPageBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class ListArchivedPage : Fragment() {

    private lateinit var binding: ListRentPageBinding
    private val viewmodel by activityViewModels<PerpusViewModel> { component.perpusVmFactory }
    private var firstLoad = true
    private val adapter by lazy { RentAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ListRentPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewmodel.loadingHistory.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewmodel.hasNextHistory = true
                viewmodel.lastStartHistory = -1
                viewmodel.getHistory()
            }
        }

        binding.rvRent.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._4sdp
                ),
                includeTop = true,
                includeBottom = true
            )
        )
        binding.rvRent.adapter = adapter

        viewmodel.listReturn.observe(viewLifecycleOwner, {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvRent.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvRent.scrollToPosition(position)
                    }
                } else {
                    binding.rvRent.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })

        binding.swipeRefresh.isRefreshing = true
    }
}