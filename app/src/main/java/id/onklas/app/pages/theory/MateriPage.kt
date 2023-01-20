package id.onklas.app.pages.theory

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.MateriPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class MateriPage : Privatepage() {

    private val binding by lazy { MateriPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<TheoryViewModel> { component.theoryVmFactory }
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
            binding.toolbar.title = intent.getStringExtra("title") ?: "Daftar Materi"
            title = intent.getStringExtra("title") ?: "Daftar Materi"
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                firstLoad = true
                viewmodel.fetchMateriBySubject(intent.getIntExtra("id", 0))
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.rvMateri.adapter = adapter
        binding.rvMateri.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ),
                includeTop = true,
                includeBottom = true,
//                customEdge = resources.getDimensionPixelSize(R.dimen._16sdp)
            )
        )

        viewmodel.listMateri(intent.getIntExtra("id", 0)).observe(this, {
            adapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager =
                        (binding.rvMateri.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvMateri.scrollToPosition(position)
                    }
                } else {
                    binding.rvMateri.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })

        viewmodel.errorString.observe(this, Observer(this::toast))

        lifecycleScope.launch { viewmodel.fetchMateriBySubject(intent.getIntExtra("id", 0)) }
    }

    private val adapter by lazy {
        MateriAdapter({ item, view ->
            startActivity(
                Intent(this@MateriPage, MateriDetailPage::class.java)
                    .putExtra("title", item.materi.name)
                    .putExtra("id", item.materi.id.toInt())
                    .putExtra("subId", item.mapel.id.toInt())
            )
        })
    }
}