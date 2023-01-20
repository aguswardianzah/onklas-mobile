package id.onklas.app.pages.akun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.databinding.PairingItemBinding
import id.onklas.app.databinding.PairingOtpDialogBinding
import id.onklas.app.databinding.PairingPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class PairingPage : Fragment() {


    private lateinit var binding: PairingPageBinding
    private val viewmodel by activityViewModels<PairingViewModel> { component.pairingVmFactory }

    private var firstLoad = true
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View = PairingPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewmodel.fetchListPairing(0)
                initData()
            }
        }
        binding.rvPairs.apply {
            adapter = listAdapter
            addItemDecoration(
                    LinearSpaceDecoration(
                            space = resources.getDimensionPixelSize(
                                    R.dimen._4sdp
                            ),
                            includeTop = true,
                            includeBottom = true
                    )
            )
        }
        viewmodel.LoadingShow.observe(viewLifecycleOwner, Observer {
            binding.swipeRefresh.isRefreshing = it
        })

        viewmodel.errorString.observe(viewLifecycleOwner, Observer {
            (requireActivity() as BasePage).toast(it)
        })

        val observer = Observer<pairingItem> {
            alertSelectNew(it.otp)
        }
        viewmodel.acceptPairingResponse.removeObserver(observer)
        viewmodel.acceptPairingResponse.observe(viewLifecycleOwner, observer)


    }

    private var currLiveData: LiveData<PagedList<PairingTable>>? = null
    private fun initData() {
        firstLoad = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)
        currLiveData = viewmodel.listPairing(
        ).apply {
            observe(viewLifecycleOwner, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<PairingTable>> {
            Timber.e("loaded data: ${it.size}")
            listAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvPairs.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvPairs.scrollToPosition(position)
                    }
                } else {
                    binding.rvPairs.scrollToPosition(0)
                    firstLoad = false
                }

            }
        }
    }
    private val listAdapter by lazy {
        object : PagingAdapter<PairingTable, ListVh>(object :
                DiffUtil.ItemCallback<PairingTable>() {
            override fun areItemsTheSame(
                    oldItem: PairingTable,
                    newItem: PairingTable
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                    oldItem: PairingTable,
                    newItem: PairingTable
            ): Boolean =
                    oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): ListVh =
                    ListVh(parent)

            override fun bindItemViewholder(holder: ListVh, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(holder: ListVh, position: Int, payloads: MutableList<Any>) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    private inner class ListVh(
            parent: ViewGroup,
            val binding: PairingItemBinding = PairingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PairingTable) {
            binding.item = item
            binding.root.setOnClickListener {
                select(item)
            }
        }

        private fun select(item: PairingTable) {
            lifecycleScope.launch {
                viewmodel.acceptPairing(item.id)
            }
        }

    }

    var alertDialog: AlertDialog? = null

    private fun alertSelectNew(
            pinText: String
    ) {
        (requireActivity() as BasePage).dismissAlert()
        val alertSelectNew = PairingOtpDialogBinding.inflate(layoutInflater).apply {
            pin.setText(pinText)
            btnClose.setOnClickListener {
                alertDialog?.dismiss()
            }
        }
        alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setView(alertSelectNew.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }
    }

}