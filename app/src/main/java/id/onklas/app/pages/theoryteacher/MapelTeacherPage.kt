package id.onklas.app.pages.theoryteacher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.MapelTeacherPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.theory.MapelAdapter
import id.onklas.app.pages.theory.MateriPage
import id.onklas.app.pages.theory.TheoryViewModel
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class MapelTeacherPage : Fragment() {

    private lateinit var binding: MapelTeacherPageBinding
    private val viewmodel by activityViewModels<TheoryViewModel> { component.theoryVmFactory }
    private var firstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        MapelTeacherPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.fetchMapel(0)
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.rvMapel.apply {
            adapter = pageAdapter
            addItemDecoration(
                LinearSpaceDecoration(
                    space = resources.getDimensionPixelSize(R.dimen._12sdp),
                    includeTop = true,
                    includeBottom = true,
                    customEdge = resources.getDimensionPixelSize(R.dimen._16sdp)
                )
            )
        }

        viewmodel.listMapel.observe(viewLifecycleOwner, {
            pageAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvMapel.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvMapel.scrollToPosition(position)
                    }
                } else {
                    binding.rvMapel.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })
        lifecycleScope.launchWhenCreated { viewmodel.fetchMapel() }
    }

    private val pageAdapter by lazy {
        MapelAdapter { item, _ ->
            startActivity(
                Intent(requireActivity(), MateriPage::class.java)
                    .putExtra("title", item.mapel.name)
                    .putExtra("id", item.mapel.id.toInt())
            )
        }
    }
}