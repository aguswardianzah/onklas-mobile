package id.onklas.app.pages.entrepreneurs.myMerchant.addProduct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.AddProductMarketingLocationItemBinding
import id.onklas.app.databinding.AddProductMarketingLocationPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.MarketingLocation

class AddProductMarketingLocationPage : Privatepage() {

    private val binding by lazy { AddProductMarketingLocationPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<EntrepreneursVM> { component.entrepreneursFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Metode Pengiriman"
            binding.toolbar.title = "Metode Pengiriman"
        }
        binding.rvMarketingLocation.apply {
            layoutManager = LinearLayoutManager(this@AddProductMarketingLocationPage, RecyclerView.VERTICAL, false)
            adapter = categoryParent(viewmodel.marketingLoactionDummy)
        }



    }

    private inner class categoryParent(
        val listProduct: List<MarketingLocation>
    ) :
        RecyclerView.Adapter<categoryParent.categoryVH>() {

        private inner class categoryVH(
            parent: ViewGroup,
            val bindingitem:AddProductMarketingLocationItemBinding = AddProductMarketingLocationItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        ) :
            RecyclerView.ViewHolder(bindingitem.root) {
            fun bind(item: MarketingLocation ) {
                bindingitem.item = item
                bindingitem.checkBox.isChecked = item.selected
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryVH =
            categoryVH(parent)

        override fun getItemCount(): Int = listProduct.size

        override fun onBindViewHolder(holder:categoryVH, position: Int) =
            holder.bind(listProduct[position])


    }
}