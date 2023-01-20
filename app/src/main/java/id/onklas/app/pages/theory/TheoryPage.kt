package id.onklas.app.pages.theory

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.TheoryPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class TheoryPage : Privatepage() {

    private val binding by lazy { TheoryPageBinding.inflate(layoutInflater) }
    private var firstLoad = true
    private val viewmodel by viewModels<TheoryViewModel> { component.theoryVmFactory }

    init {
        lifecycleScope.launchWhenCreated { viewmodel.fetchMapel() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

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

        viewmodel.listMapel.observe(this, Observer {
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

        viewmodel.errorString.observe(this, Observer(this::toast))
    }

    private val pageAdapter by lazy {
        MapelAdapter { item, view ->
            startActivity(
                Intent(this, MateriPage::class.java)
                    .putExtra("title", item.mapel.name)
                    .putExtra("id", item.mapel.id.toInt())
            )
        }
    }
}