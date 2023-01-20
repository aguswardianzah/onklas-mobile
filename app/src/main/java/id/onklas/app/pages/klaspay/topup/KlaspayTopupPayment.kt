package id.onklas.app.pages.klaspay.topup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayTopupSelectMethodPageBinding
import id.onklas.app.di.component
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch
import timber.log.Timber

class KlaspayTopupPayment : Fragment() {

    //    private lateinit var binding: KlaspayTopupPaymentBinding
    private lateinit var binding: KlaspayTopupSelectMethodPageBinding
    private val viewModel by activityViewModels<KlaspayTopupViewModel> { component.KlaspayTopupVmFactory }

    init {
        lifecycleScope.launchWhenCreated {
            viewModel.klaspayWallet()
            viewModel.klaspayToolbar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        KlaspayTopupSelectMethodPageBinding.inflate(inflater, container, false)
            .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.titleBar.postValue("Top Up")

        binding.rvTopup.apply {
            adapter = this@KlaspayTopupPayment.adapter
            addItemDecoration(
                LinearSpaceDecoration(
                    space = resources.getDimensionPixelSize(R.dimen._6sdp),
                    includeTop = true,
                    includeBottom = true
                )
            )
        }

        viewModel.balance.observe(viewLifecycleOwner, {
            binding.textKlaspay.text = it
        })

        viewModel.paymentMethodList.observe(viewLifecycleOwner) {
            Timber.e("list: ${it.map { "${it.nameId} -- ${it.showChild}" }}")
            adapter.submitList(it?.toMutableList())
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.loadingList.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.klaspayToolbar()
            }
        }

        binding.executePendingBindings()
    }

    private val adapter by lazy {
        TopUpAdapter({ item, position ->
            viewModel.paymentMethodList.value?.let { currentList ->
                val newList = ArrayList(currentList)
                val newItem = currentList.first { it.nameId == item.nameId }.copy()
                val newShowChild = !newItem.showChild

                val index = newList.indexOf(newItem)
                if (index > -1)
                    newList[index] = newItem.apply { showChild = newShowChild }

                if (newShowChild)
                    newList.addAll(position + 1, item.items)
                else
                    newList.removeAll { it.parentName == item.nameId }

                viewModel.paymentMethodList.postValue(newList)
            }
        }, { item ->
            viewModel.channelSelected.postValue(item)
            findNavController().navigate(R.id.action_klaspayTopupPayment_to_klaspayTopupNominal)
        })
    }
}