package id.onklas.app.pages.entrepreneurs.review

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.databinding.ReviewItemBinding
import id.onklas.app.databinding.ReviewServiceListBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.pembelian.review.AddReviewBuyerPage


class ReviewServiceList : Fragment() {

    private lateinit var binding: ReviewServiceListBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return ReviewServiceListBinding.inflate(inflater, container, false).also { binding=it }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launchWhenCreated { viewmodel.loadDummyList() }

        viewmodel.dummyList.observe(requireActivity(), Observer {
            ServiceAdapter.submitList(it)
        })

        binding.rvService.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = ServiceAdapter
        }
    }


    private val ServiceAdapter by lazy {
        object : ListAdapter<String, ServiceViewholder>(object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewholder =
                ServiceViewholder(parent)

            override fun onBindViewHolder(holder:ServiceViewholder, position: Int) =
                holder.bind(getItem(position),position)

        }
    }
    private inner class ServiceViewholder(
        parent: ViewGroup,
        val binding: ReviewItemBinding = ReviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String,position: Int) {
            binding.imgProduct.clipToOutline = true
            glide.load("https://placeimg.com/100/100/any")
                .centerCrop()
                .into(binding.imgProduct)

            binding.root.setOnClickListener {
                val i = Intent(requireContext(),
                    AddReviewBuyerPage::class.java)
                i.putExtra("jenis","jasa")
                startActivity(i)
            }

        }

    }
}