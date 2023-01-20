package id.onklas.app.pages.jelajah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.JelajahHashtagItemBinding
import id.onklas.app.databinding.JelajahRvPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.sekolah.sosmed.HashtagTable
import id.onklas.app.utils.LinearSpaceDecoration

class JelajahHashtagPage : Fragment() {

    private lateinit var binding: JelajahRvPageBinding
    private val viewmodel by activityViewModels<JelajahViewModel> { component.jelajahVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        JelajahRvPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.rvJelajah.apply {
            layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            addItemDecoration(
                LinearSpaceDecoration(
                    space = resources.getDimensionPixelSize(R.dimen._8sdp),
                    includeTop = true,
                    includeBottom = true
                )
            )
            adapter = this@JelajahHashtagPage.adapter
        }

        viewmodel.search.observe(viewLifecycleOwner, {
            lifecycleScope.launchWhenCreated {
                viewmodel.listHashtag(it).observe(viewLifecycleOwner, {
                    adapter.submitList(it)
                })
                viewmodel.hasMoreHashtag = true
                viewmodel.isRequestHashtag = false
                viewmodel.fetchHashtag(0, it)
            }
        })

        viewmodel.loadingHashtag.observe(viewLifecycleOwner, Observer(binding::setLoading))
    }

    private val adapter by lazy {
        object : PagedListAdapter<HashtagTable, Viewholder>(
            object : DiffUtil.ItemCallback<HashtagTable>() {
                override fun areItemsTheSame(
                    oldItem: HashtagTable,
                    newItem: HashtagTable
                ): Boolean = oldItem.name.toLowerCase() == newItem.name.toLowerCase()

                override fun areContentsTheSame(
                    oldItem: HashtagTable,
                    newItem: HashtagTable
                ): Boolean = oldItem == newItem
            }
        ) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    inner class Viewholder(
        parent: ViewGroup,
        val binding: JelajahHashtagItemBinding = JelajahHashtagItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HashtagTable) {
            binding.item = item
            binding.executePendingBindings()
            val hashtag = "#" + item.name
            binding.root.setOnClickListener { HashtagPostPage.open(requireActivity(), hashtag) }
            binding.image.setOnClickListener { HashtagPostPage.open(requireActivity(), hashtag) }
            binding.name.setOnClickListener { HashtagPostPage.open(requireActivity(), hashtag) }
            binding.username.setOnClickListener {
                HashtagPostPage.open(
                    requireActivity(),
                    hashtag
                )
            }
        }
    }
}