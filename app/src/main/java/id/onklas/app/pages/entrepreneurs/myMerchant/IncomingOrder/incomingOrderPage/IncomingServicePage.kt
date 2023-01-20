package id.onklas.app.pages.entrepreneurs.myMerchant.IncomingOrder.incomingOrderPage

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import id.onklas.app.databinding.IncomingServicePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM
import id.onklas.app.pages.entrepreneurs.OrderDetailPage


class IncomingServicePage : Fragment() {

    private lateinit var binding: IncomingServicePageBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = IncomingServicePageBinding.inflate(inflater, container, false).also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.kosongLay.txtTitle.text = "Belum Ada Transaksi"
        binding.kosongLay.txtSubtitle.text = "Upload produk dan berilah deskripsi yang lengkap untuk menarik pembeli\n"

        lifecycleScope.launchWhenCreated { viewmodel.loadDummyList() }

        viewmodel.dummyList.observe(requireActivity(), Observer {
            IncomingAdapter.submitList(it)
        })

        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = IncomingAdapter
        }
    }


    private val IncomingAdapter by lazy {
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

            binding.declineService.root.visibility = View.VISIBLE
            binding.declineService.btnTolak.setOnClickListener {
                button2Dialog()
            }


            binding.imgProduct.clipToOutline = true
            glide.load("https://placeimg.com/100/100/any")
                    .centerCrop()
                    .into(binding.imgProduct)
            binding.actionDetail.setOnClickListener {
                val i = Intent(requireContext(), OrderDetailPage::class.java)
                i.putExtra("title", "Detail Orderan Jasa Masuk")
                i.putExtra("position", "1")
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

        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_danger) }
//        bindingDialog.imgDialog.apply { setBackgroundResource(R.drawable.img_pay_success) }

        bindingDialog.txtTitle.text = "Tolak Orderan"
        bindingDialog.txtDesc.text = "Orderan yang ditolak berpotensi mengecewakan pembeli"

        bindingDialog.button2.root.visibility = View.VISIBLE
        bindingDialog.button2.btnDone.text = "Tolak Orderan"
        bindingDialog.button2.btnAction.text = "Batalkan"
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

        bindingDialog.txtTitle.text = "Orderan Sudah Ditolak"
        bindingDialog.txtDesc.text = "Orderan Masuk dari Ninda Rahmah sudah berhasil dibatalkan "

        bindingDialog.button1.root.visibility = View.VISIBLE
        bindingDialog.button1.btnDone.text = "Oke Terimakasih"
        bindingDialog.button1.btnDone.setOnClickListener {
            dialog.dismiss()
        }

    }
}