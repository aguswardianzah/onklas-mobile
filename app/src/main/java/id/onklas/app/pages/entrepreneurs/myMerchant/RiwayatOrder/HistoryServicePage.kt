package id.onklas.app.pages.entrepreneurs.myMerchant.RiwayatOrder

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
import id.onklas.app.databinding.HistoryServicePageBinding
import id.onklas.app.databinding.IncomingOrderServiceItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.OrderDetailPage

class HistoryServicePage : Fragment() {

    private lateinit var binding: HistoryServicePageBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = HistoryServicePageBinding.inflate(inflater, container, false).also { binding = it}.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launchWhenCreated { viewmodel.loadDummyList() }

        viewmodel.dummyList.observe(requireActivity(), Observer {
            serviceAdapter.submitList(it)
        })

        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = serviceAdapter
        }
    }

    private val serviceAdapter by lazy {
        object : ListAdapter<String, ServiceViewholder>(object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewholder =
                ServiceViewholder(parent)

            override fun onBindViewHolder(holder: ServiceViewholder, position: Int) =
                holder.bind(getItem(position),position)

        }
    }
    private inner class ServiceViewholder(
        parent: ViewGroup,
        val binding: IncomingOrderServiceItemBinding = IncomingOrderServiceItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String,position: Int) {

            if (position%2==0){
                binding.penjualanSelesai.root.visibility =View.VISIBLE
            }else{
                binding.penjualanDitolak.root.visibility =View.VISIBLE
            }


            binding.imgProduct.clipToOutline = true
            glide.load("https://placeimg.com/100/100/any")
                .centerCrop()
                .into(binding.imgProduct)

            binding.root.setOnClickListener {
                val i = Intent(requireContext(), OrderDetailPage::class.java)
                i.putExtra("title","Detail Orderan Diproses")
                i.putExtra("position","3")
                startActivity(i)
            }
        }

    }


}