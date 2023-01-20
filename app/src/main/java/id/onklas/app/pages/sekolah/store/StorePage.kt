package id.onklas.app.pages.sekolah.store

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.onklas.app.R
import id.onklas.app.databinding.StoreFilterBinding
import id.onklas.app.databinding.StoreFilterItemBinding
import id.onklas.app.databinding.StorePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.chat.ChatListPage
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import kotlinx.coroutines.launch
import timber.log.Timber

class StorePage : Fragment() {

    private lateinit var binding: StorePageBinding
    private val viewmodel by activityViewModels<StoreVm> { component.storeVmFactory }
    private val viewmodelKWU by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }

    private val viewModelCart by activityViewModels<CartViewModel> { component.cartVmFactory }

    private val navController by lazy {
        Navigation.findNavController(
            requireActivity(),
            R.id.store_nav_controller
        )
    }

    private var listFilter = ArrayList<StoreFilterModel>()
    private var filterAdapter: FilterAdapter? = null

    private var filterSeleced = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = StorePageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.inflateMenu(R.menu.menu_home_store)

        val actionCart = binding.toolbar.menu.findItem(R.id.menu_cart).actionView
        val counterCart = actionCart.findViewById<TextView>(R.id.count)
        viewModelCart.countCart.observe(viewLifecycleOwner, {
            counterCart.text = if (it < 100) it.toString() else "99+"
        })
        actionCart.setOnClickListener {
            CartPage.open(requireActivity())
        }

        val actionChat = binding.toolbar.menu.findItem(R.id.menu_chat).actionView
        actionChat.setOnClickListener {
            startActivity(
                Intent(requireActivity(), ChatListPage::class.java).putExtra(
                    "type",
                    "kwu"
                )
            )
        }

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_cart)
                CartPage.open(requireActivity())
            else
                startActivity(
                    Intent(requireActivity(), ChatListPage::class.java).putExtra(
                        "type",
                        "kwu"
                    )
                )
            true
        }

        binding.searchLay.setOnClickListener {
            startActivity(Intent(requireContext(), StoreSearchPage::class.java))
        }
        binding.inName.setOnClickListener {
            startActivity(Intent(requireContext(), StoreSearchPage::class.java))
        }

        lifecycleScope.launchWhenCreated {
            viewModelCart.fetchCart()
            val count = viewModelCart.db.cart().countCart()
            viewModelCart.countCart.postValue(count)

            viewmodelKWU.loadMerchantUser()
        }

        listFilter.addAll(viewmodel.storeFilter)
        filterAdapter = FilterAdapter(listFilter)
    }

    override fun onResume() {
        super.onResume()

        try {
            // update cart count
            lifecycleScope.launchWhenCreated {
                viewModelCart.countCart.postValue(viewmodel.db.cart().countCart())
            }

            // update chat count
            if (viewmodel.pref.getBoolean("klaspayActive") && binding.toolbar.menu.findItem(R.id.menu_chat) == null) {
                viewmodel.db.chat().countAllUnread("kwu").observe(viewLifecycleOwner) {
                    try {
                        if (it == null) return@observe

                        Timber.e("chat unread: $it")
                        val actionChat = binding.toolbar.menu.findItem(R.id.menu_chat).actionView
                        val badge = actionChat.findViewById<TextView>(R.id.count)
                        badge.text = if (it < 100) it.toString() else "99+"
                        badge.visibility = if (it > 0) View.VISIBLE else View.GONE
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    @SuppressLint("SetTextI18n")
    fun bottomSeetFilter(layoutInflater: LayoutInflater) {
        val dialog = BottomSheetDialog(requireContext())
        val bindingHomeFilter by lazy { StoreFilterBinding.inflate(layoutInflater) }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = bindingHomeFilter.root

        bindingHomeFilter.rvUrutanProduk.apply {
            layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
            adapter = filterAdapter
        }
        bindingHomeFilter.actionExit.setOnClickListener {
            dialog.dismiss()
        }
        bindingHomeFilter.actionTampilkanProduk.setOnClickListener {
            if (listFilter.find { it.is_selected } != null) {
                val Item = listFilter.find { it.is_selected }
                val posisiItem = listFilter.indexOf(Item)
                listFilter[posisiItem].filter_code
                dialog.dismiss()
                navController.navigate(R.id.action_global_storeSearchResult)
            }
        }

        viewmodel.LoadingShow.observe(viewLifecycleOwner) {
            if (it) {
                bindingHomeFilter.labelLoading.visibility = View.VISIBLE
            } else {
                bindingHomeFilter.labelLoading.visibility = View.GONE
            }
        }

        viewmodel.CountProductFilter.observe(this) {
            bindingHomeFilter.actionTampilkanProduk.text = "Tampilkan $it Produk"
        }

        bindingHomeFilter.actionTampilkanProduk.setOnClickListener {
            dialog.dismiss()

            if (listFilter.find { it.is_selected } != null) {
                val Item = listFilter.find { it.is_selected }
                val posisiItem = listFilter.indexOf(Item)
                val data = listFilter[posisiItem]

                val bundle = bundleOf("selectedFilter" to data.filter_code)
                navController.navigate(R.id.action_global_storeSearchResult, bundle)
            }
        }

        bindingHomeFilter.actionReset.setOnClickListener {
            listFilter.clear()
            listFilter.addAll(viewmodel.storeFilter)
            filterAdapter?.notifyDataSetChanged()
        }

        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val bottomSheetDialog = dialog
        val parentLayout =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            setupFullHeight(it)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.show()
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
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
            }

            bindingitem.rdFilter.setOnClickListener {
                //reset value
                if (listFilter.find { it.is_selected } != null) {
                    val Item = listFilter.find { it.is_selected }
                    val posisiItem = listFilter.indexOf(Item)
                    val olddata = listFilter[posisiItem]
                    listFilter.set(
                        posisiItem, StoreFilterModel(
                            olddata.id,
                            olddata.name,
                            olddata.filter_code,
                            false
                        )
                    )
                    filterAdapter?.notifyItemChanged(posisiItem)
                }

                //add new value
                listFilter.set(
                    position, StoreFilterModel(
                        item.id,
                        item.name,
                        item.filter_code,
                        true
                    )
                )
                viewmodel.selectedFilter.postValue(item.filter_code)
                filterSeleced = item.filter_code
                filterAdapter?.notifyItemChanged(position)

            }
        }
    }


}