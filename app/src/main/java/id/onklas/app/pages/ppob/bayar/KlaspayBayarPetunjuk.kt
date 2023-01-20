package id.onklas.app.pages.ppob.bayar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayBayarPetunjukBinding
import id.onklas.app.databinding.KlaspayGuidanceItemBinding
import id.onklas.app.databinding.KlaspayGuidanceTypeItemBinding
import id.onklas.app.databinding.KlaspayTopupFinishItemBinding
import id.onklas.app.di.component

class KlaspayBayarPetunjuk : Fragment() {

    private lateinit var binding: KlaspayBayarPetunjukBinding
    private val viewModel by activityViewModels<KlaspayBayarViewModel> { component.klaspayBayarVmFactory }
    private var listPetunjukBayar: List<String> = emptyList()
    private var totalBayar: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        KlaspayBayarPetunjukBinding.inflate(inflater, container, false).also { binding = it }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        viewModel.titleBar.postValue("Selesaikan Pembayaran")

        viewModel.bayarData.observe(viewLifecycleOwner, {
            binding.data = it
            binding.petunjuk = "Segera transfer sebelum ${viewModel.dateFormat(it.expired_date)} ${
                viewModel.timeFormat(it.expired_date)
            } agar tidak kadaluarsa. Perbedaan nilai transfer dapat menghambat proses transaksi pembayaran Anda."
            binding.totalAmount = "Rp ${viewModel.numberUtil.formatCurrency(it.total_amount!!)}"
            totalBayar = it.total_amount.toString()
        })

        viewModel.guidanceList.observe(viewLifecycleOwner, {
            listPetunjukBayar = it
            binding.listPetunjuk.adapter = adapterPetunjukBayar
        })

        binding.buttonNumberCopy.setOnClickListener {
            viewModel.copyText(requireActivity(), binding.textNumber.text.toString())
        }
        binding.buttonTotalCopy.setOnClickListener {
            viewModel.copyText(requireActivity(), totalBayar)
        }
        binding.textDetail.setOnClickListener {
            findNavController().navigate(R.id.action_klaspayBayarPetunjuk_to_klaspayBayarDetail)
        }

        binding.labelPetunjuk.setOnClickListener {
            if (binding.listPetunjuk.isGone) {
                binding.dividerPentunjuk.visibility = View.VISIBLE
                binding.listPetunjuk.visibility = View.VISIBLE
                binding.imgSwitch.rotation = 180F
            } else {
                binding.dividerPentunjuk.visibility = View.GONE
                binding.listPetunjuk.visibility = View.GONE
                binding.imgSwitch.rotation = 0F
            }
        }

//        listPetunjukBayar = listOf(
//            "Masukkan kartu ATM dan PIN kamu",
//            "Pilih ke menu BAYAR/BELI > LAINNYA > LAINNYA > pilih e-Commerce",
//            "Masukkan kode perusahaan untuk Klaspay : 30001 ",
//            "Masukkan nomor HP Anda yang terdaftar pada aplikasi Klaspay",
//            "Masukkan jumlah Klaspay yang ingin diisi",
//            "Ikuti petunjuk selanjutnya untuk menyelesaikan proses pengisian Klaspay",
//        )
//        binding.listPetunjuk.adapter = adapterPetunjukBayar
    }

//    private val adapter by lazy {
//        object : ListAdapter<TypeGuidanceItem, RecyclerView.ViewHolder>(object :
//                DiffUtil.ItemCallback<TypeGuidanceItem>() {
//            override fun areItemsTheSame(oldItem: TypeGuidanceItem, newItem: TypeGuidanceItem): Boolean =
//                    oldItem.paymentMethod == newItem.paymentMethod
//
//            override fun areContentsTheSame(oldItem: TypeGuidanceItem, newItem: TypeGuidanceItem): Boolean =
//                    oldItem.showChild == newItem.showChild
//
//            override fun getChangePayload(oldItem: TypeGuidanceItem, newItem: TypeGuidanceItem): Any =
//                    newItem.showChild
//        }) {
//            override fun getItemViewType(position: Int): Int =
//                    if (getItem(position).parentName.isEmpty()) 0 else 1
//
//            override fun onCreateViewHolder(
//                    parent: ViewGroup,
//                    viewType: Int
//            ): RecyclerView.ViewHolder =
//                    if (viewType == 0) TypeViewholder(parent) else ItemViewholder(parent)
//
//            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//                val item = getItem(position)
//                if (holder is TypeViewholder) holder.bind(item)
//                else if (holder is ItemViewholder) holder.bind(item)
//            }
//
//            override fun onBindViewHolder(
//                    holder: RecyclerView.ViewHolder,
//                    position: Int,
//                    payloads: MutableList<Any>
//            ) {
//                if (payloads.isNotEmpty() && holder is TypeViewholder) {
//                    Timber.e("payload ${getItem(position).nameId}: $payloads")
//                    holder.update(payloads.first() as Boolean)
//                } else {
//                    onBindViewHolder(holder, position)
//                }
//            }
//        }
//    }

    private inner class TypeViewholder(
        parent: ViewGroup,
        val binding: KlaspayGuidanceTypeItemBinding = KlaspayGuidanceTypeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TypeGuidanceItem) {
            binding.item = item
            binding.showChild = item.showChild
            binding.container.setOnClickListener {
//                viewModel.paymentMethodList.value?.let { currentList ->
//                    val newList = ArrayList(currentList)
//                    val newItem = currentList.first { it.nameId == item.nameId }.copy()
//                    val newShowChild = !newItem.showChild
//
//                    val index = newList.indexOf(newItem)
//                    if (index > -1)
//                        newList[index] = newItem.apply { showChild = newShowChild }
//
//                    if (newShowChild)
//                        newList.addAll(adapterPosition + 1, item.items)
//                    else
//                        newList.removeAll { it.parentName == item.nameId }
//
//                    viewModel.paymentMethodList.postValue(newList)
//                }
            }
            binding.executePendingBindings()
        }

        fun update(showChild: Boolean) {
            binding.showChild = showChild
            binding.executePendingBindings()
        }
    }

    private inner class ItemViewholder(
        parent: ViewGroup,
        val binding: KlaspayGuidanceItemBinding = KlaspayGuidanceItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.name = item
            binding.executePendingBindings()
        }
    }


    private val adapterPetunjukBayar by lazy {
        object : RecyclerView.Adapter<KlaspayBayarPetunjuk.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(
                    parent
                )

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                holder.bind(listPetunjukBayar[position], position)
            }

            override fun getItemCount() = listPetunjukBayar.size
        }
    }

    private inner class ViewHolder(
        parent: ViewGroup,
        val binding: KlaspayTopupFinishItemBinding = KlaspayTopupFinishItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(petunjuk: String, index: Int) {
            binding.textNumber.text = (index + 1).toString()
            binding.textContent.text = petunjuk
        }
    }
}