package id.onklas.app.pages.jelajah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.JelajahRvPageBinding
import id.onklas.app.databinding.JelajahUserItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.akun.ProfilePage
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter

class JelajahUserPage : Fragment() {

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
            adapter = this@JelajahUserPage.adapter
        }

        viewmodel.search.observe(viewLifecycleOwner, {
            lifecycleScope.launchWhenCreated {
                viewmodel.listUser(it).observe(viewLifecycleOwner, Observer(adapter::submitList))
                viewmodel.hasMoreUser = true
                viewmodel.isRequestUser = false
                viewmodel.fetchUser(0, it)
            }
        })

        viewmodel.loadingUser.observe(viewLifecycleOwner, Observer(binding::setLoading))
    }

    private val onUserClick by lazy {
        { item: UserTable ->
            ProfilePage.open(
                requireActivity(),
                nisn_nik = if (item.nisn_nik.isNotEmpty()) item.nisn_nik else item.nis_nik
            )
        }
    }

    private val adapter by lazy {
        object : PagingAdapter<UserTable, Viewholder>(
            object : DiffUtil.ItemCallback<UserTable>() {
                override fun areItemsTheSame(
                    oldItem: UserTable,
                    newItem: UserTable
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: UserTable,
                    newItem: UserTable
                ): Boolean = oldItem == newItem
            }
        ) {
            override fun createItemViewholder(
                parent: ViewGroup,
                viewType: Int
            ): Viewholder = Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it, onUserClick) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) {
                getItem(position)?.let { holder.bind(it, onUserClick) }
            }
        }
    }

    inner class Viewholder(
        parent: ViewGroup,
        val binding: JelajahUserItemBinding = JelajahUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserTable, onClick: (item: UserTable) -> Unit) {
            binding.item = item
            binding.executePendingBindings()

            binding.root.setOnClickListener { onClick.invoke(item) }
            binding.image.setOnClickListener { onClick.invoke(item) }
            binding.name.setOnClickListener { onClick.invoke(item) }
            binding.username.setOnClickListener { onClick.invoke(item) }
        }
    }
}