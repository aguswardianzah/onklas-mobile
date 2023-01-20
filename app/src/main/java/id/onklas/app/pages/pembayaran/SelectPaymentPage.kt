package id.onklas.app.pages.pembayaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.PaymentTypeItemBinding
import id.onklas.app.databinding.SelectPaymentPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PageDialogFragment
import kotlinx.coroutines.launch

class SelectPaymentPage(val onClick: (PaymentType) -> Unit) : PageDialogFragment() {

    private val viewmodel by activityViewModels<PaymentViewModel> { component.paymentVmFactory }
    private val items by lazy { ArrayList<PaymentType>() }
    private lateinit var binding: SelectPaymentPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        SelectPaymentPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.rvPaymentType.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )
        binding.rvPaymentType.adapter = adapter

        lifecycleScope.launch {
            items.clear()
            items.addAll(viewmodel.fetchPaymentType())
            adapter.notifyDataSetChanged()
            binding.progress.visibility = View.GONE
        }
    }

    private val adapter by lazy {
        object : RecyclerView.Adapter<Viewholder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun getItemCount(): Int = items.size

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(items[position])
        }
    }

    private inner class Viewholder(
        parent: ViewGroup,
        val binding: PaymentTypeItemBinding = PaymentTypeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentType) {
            binding.label.text = item.name
            binding.root.setOnClickListener {
                onClick.invoke(item)
                dismiss()
            }
        }
    }
}