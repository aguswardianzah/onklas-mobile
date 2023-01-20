package id.onklas.app.pages.sekolah.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.databinding.StoreFilterBinding
import id.onklas.app.databinding.StoreFilterItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch

class StoreFilter : Privatepage() {

    companion object {
        fun open(activity: AppCompatActivity) {
            activity.startActivity(
                    Intent(activity, StoreFilter::class.java)
            )
        }
    }

    private val binding by lazy { StoreFilterBinding.inflate(layoutInflater) }
    private val glide by lazy { GlideApp.with(this) }
    private val viewmodel by viewModels<StoreVm> { component.storeVmFactory }

    private var listFilter = ArrayList<StoreFilterModel>()
    private var filterAdapter: FilterAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowHomeEnabled(true)
//            binding.toolbar.setNavigationOnClickListener { finish() }
//            title = "Filter"
//            binding.toolbar.title = "Filter"
//        }
        listFilter.addAll(viewmodel.storeFilter)

        filterAdapter = FilterAdapter(listFilter)



        binding.rvUrutanProduk.apply {
            layoutManager = LinearLayoutManager(this@StoreFilter, RecyclerView.VERTICAL, false)
            adapter = filterAdapter
        }


    }

    private inner class FilterAdapter(
            val list: List<StoreFilterModel>
    ) :
            RecyclerView.Adapter<CategoryChildViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryChildViewHolder =
                CategoryChildViewHolder(parent)

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: CategoryChildViewHolder, position: Int) =
                holder.bind(position, list[position])


    }

    private inner class CategoryChildViewHolder(
            parent: ViewGroup,
            val bindingitem: StoreFilterItemBinding = StoreFilterItemBinding.inflate(
                    LayoutInflater.from(
                            parent.context
                    ), parent, false
            )
    ) :
            RecyclerView.ViewHolder(bindingitem.root) {
        fun bind(position: Int, item: StoreFilterModel) {
            bindingitem.filterName.text = item.name
            bindingitem.rdFilter.isChecked = item.is_selected

            if (item.is_selected) {
                lifecycleScope.launch { viewmodel.loadCountProductFilter(item.filter_code) }
                viewmodel.CountProductFilter.observe(this@StoreFilter, Observer {
                    binding.actionTampilkanProduk.text = "Tampilkan $it Produk"
                })
            }

            binding.actionTampilkanProduk.setOnClickListener {
                if (listFilter.find { it.is_selected } != null) {
                    val Item = listFilter.find { it.is_selected }
                    val posisiItem = listFilter.indexOf(Item)
                    val intent = Intent("OnklasBroadcast")
                    intent.putExtra("openResult", "openResult")
                    intent.putExtra("filterCode", listFilter[posisiItem].filter_code)

                    LocalBroadcastManager.getInstance(this@StoreFilter).sendBroadcast(intent)//local broadcast
                    finish()
                }

            }


            bindingitem.rdFilter.setOnClickListener {
                if (listFilter.find { it.is_selected } != null) {
                    val Item = listFilter.find { it.is_selected }
                    val posisiItem = listFilter.indexOf(Item)
                    val olddata = listFilter[posisiItem]
                    listFilter.set(posisiItem, StoreFilterModel(
                            olddata.id,
                            olddata.name,
                            olddata.filter_code,
                            false
                    ))
                    filterAdapter?.notifyItemChanged(posisiItem)
                }
                listFilter.set(position, StoreFilterModel(
                        item.id,
                        item.name,
                        item.filter_code,
                        true
                ))
                filterAdapter?.notifyItemChanged(position)

            }
        }
    }
}