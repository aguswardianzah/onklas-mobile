package id.onklas.app.pages.entrepreneurs.myMerchant.IncomingOrder.procesedOrderPage

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.EntrepreneursCostomDialogBinding
import id.onklas.app.databinding.IncomingOrderServiceItemBinding
import id.onklas.app.databinding.ProcessedServiceOrderPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.OrderDetailPage


class ProcessedServiceOrderPage : Fragment() {

    private lateinit var binding: ProcessedServiceOrderPageBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }
    private val colorWhite by lazy {
        ResourcesCompat.getColor(
                resources,
                R.color.white,
                null
        )
    }
    private val colorPrimary by lazy {

        ResourcesCompat.getColor(
                resources,
                R.color.colorPrimary,
                null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = ProcessedServiceOrderPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.kosongLay.txtTitle.text = "Belum Ada Transaksi"
        binding.kosongLay.txtSubtitle.text = "Upload produk dan berilah deskripsi yang lengkap untuk menarik pembeli\n"


        lifecycleScope.launchWhenCreated { viewmodel.loadDummyList() }

        viewmodel.dummyList.observe(requireActivity(), Observer {
            ServiceAdapter.submitList(it)
        })

        binding.rvServices.apply {
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

            override fun onBindViewHolder(holder: ServiceViewholder, position: Int) =
                    holder.bind(getItem(position), position)

        }
    }

    private inner class ServiceViewholder(
            parent: ViewGroup,
            val binding: IncomingOrderServiceItemBinding = IncomingOrderServiceItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, position: Int) {
            binding.processedService.root.visibility = View.VISIBLE

            if (position % 2 == 0) {
                binding.processedService.btnKonfirmasiSelesai.apply {
                    setTextColor(colorWhite)
                    setBackgroundResource(R.drawable.fill_blue_radius40)
                    text = "Review Penjualan"

                }

            } else {
                binding.processedService.btnKonfirmasiSelesai.apply {
                    setTextColor(colorPrimary)
                    setBackgroundResource(R.drawable.border_blue_radius40)
                }
                binding.processedService.btnKonfirmasiSelesai.setOnClickListener {
                    button2Dialog()
                }
            }








            binding.imgProduct.clipToOutline = true
            glide.load("https://placeimg.com/100/100/any")
                    .centerCrop()
                    .into(binding.imgProduct)

            binding.actionDetail.setOnClickListener {
                val i = Intent(requireContext(), OrderDetailPage::class.java)
                i.putExtra("title", "Detail Orderan Diproses")
                i.putExtra("position", "3")
                startActivity(i)
            }
        }

    }

    private fun button2Dialog() {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
                .setView(bindingDialog.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }


        //        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }

        bindingDialog.txtTitle.text = "Konfirmasi Penyelesaian"
        bindingDialog.txtDesc.text = "Apa kamu sudah menyelesaikan pekerjaan sesuai deskripsi jasa ?"

        bindingDialog.button2.root.visibility = View.VISIBLE
        bindingDialog.button2.btnDone.text = "Sudah"
        bindingDialog.button2.btnAction.text = "Belum"
        bindingDialog.button2.btnAction.setOnClickListener {
            dialog.dismiss()
            button1Dialog()
        }
        bindingDialog.button2.btnDone.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun button1Dialog() {
        val bindingDialog = EntrepreneursCostomDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
                .setView(bindingDialog.root)
                .show()
                .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }


        //        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }

        bindingDialog.txtTitle.text = "Pekerjaan Telah Diselesaikan"
        bindingDialog.txtDesc.text = "Kami akan memberitahukan kepada pembeli " +
                "bahwa bahwa pekerjaanmu sudah selesai "


        bindingDialog.button1.root.visibility = View.VISIBLE
        bindingDialog.button1.btnDone.text = "Oke Terimakasih"
        bindingDialog.button1.btnDone.setOnClickListener {
            dialog.dismiss()
        }

    }


}