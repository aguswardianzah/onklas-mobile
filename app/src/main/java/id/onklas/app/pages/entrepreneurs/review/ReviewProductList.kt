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
import id.onklas.app.databinding.IncomeProductItemBinding
import id.onklas.app.databinding.ProductPageBinding
import id.onklas.app.databinding.ReviewItemBinding
import id.onklas.app.databinding.ReviewProductListBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM


class ReviewProductList : Fragment() {

    private lateinit var binding: ReviewProductListBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return ReviewProductListBinding.inflate(inflater, container, false).also { binding=it }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launchWhenCreated { viewmodel.loadDummyList() }

        viewmodel.dummyList.observe(requireActivity(), Observer {
            ProductAdapter.submitList(it)
        })

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = ProductAdapter
        }

    }

    private val ProductAdapter by lazy {
        object : ListAdapter<String, ProductViewholder>(object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewholder =
                ProductViewholder(parent)

            override fun onBindViewHolder(holder:ProductViewholder, position: Int) =
                holder.bind(getItem(position),position)

        }
    }
    private inner class ProductViewholder(
        parent: ViewGroup,
        val binding: IncomeProductItemBinding = IncomeProductItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String,position: Int) {
//            binding.imgProduct.clipToOutline = true
//            glide.load("https://placeimg.com/100/100/any")
//                .centerCrop()
//                .into(binding.imgProduct)
//
//            binding.root.setOnClickListener {
//                val i = Intent(requireContext(),AddReviewPage::class.java)
//                i.putExtra("jenis","produk")
//                startActivity(i)
//            }
        }

    }

}