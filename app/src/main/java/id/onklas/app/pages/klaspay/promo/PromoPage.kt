package id.onklas.app.pages.klaspay.promo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import id.onklas.app.R
import id.onklas.app.databinding.PromoPageBinding
import id.onklas.app.pages.Privatepage

class PromoPage : Privatepage() {

    private val binding by lazy { PromoPageBinding.inflate(layoutInflater) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

//        binding.swipeRefresh.setOnRefreshListener {
//            lifecycleScope.launch {
//                delay(2000)
//                binding.swipeRefresh.isRefreshing = false
//            }
//        }
//
//        binding.rvPromo.addItemDecoration(
//            LinearSpaceDecoration(
//                space = R.dimen._16sdp,
//                includeTop = true,
//                includeBottom = true
//            )
//        )
//        binding.rvPromo.adapter = adapter

        binding.image.setImageResource(R.drawable.img_cashback_klaspay_horizontal)
        binding.title.text = "Promo Selalu Ada Cashback Klaspay"
        binding.time.text = "22 Februari 2021"
        binding.content.text =
            "Dapatkan cashback 1.500 rupiah untuk setiap transaksi pembayaran tagihan\n" +
                    "*berlaku untuk tagihan PLN, PDAM, TELKOM INDIHOME serta BPJS"

        binding.promoItem.clipToOutline = true
        binding.promoItem.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DetailPromoPage::class.java
                )
            )
        }
    }

//    private val item by lazy {
//        AnnouncementTable(
//            img_res = R.drawable.img_cashback_klaspay_horizontal,
//            title = "Promo Selalu Ada Cashback Klaspay",
//            created_label = "22 Februari 2021",
//            body = "Dapatkan cashback 1.500 rupiah untuk setiap transaksi pembayaran tagihan <br>" +
//                    "*berlaku untuk tagihan PLN, PDAM, TELKOM INDIHOME serta BPJS"
//        )
//    }
//    private val adapter by lazy {
//        object : RecyclerView.Adapter<Viewholder>() {
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
//                Viewholder(parent)
//
//            override fun onBindViewHolder(holder: Viewholder, position: Int) {
//                holder.bind(item)
//            }
//
//            override fun getItemCount(): Int = 10
//        }
//    }
//
//    private inner class Viewholder(
//        parent: ViewGroup,
//        val binding: AnnouncementItemBinding = AnnouncementItemBinding.inflate(
//            LayoutInflater.from(parent.context), parent, false
//        )
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        init {
//            binding.root.clipToOutline = true
//        }
//
//        fun bind(item: AnnouncementTable) {
//            binding.item = item
//            binding.image.setOnClickListener { select(item) }
//            binding.readMore.setOnClickListener { select(item) }
//            binding.title.setOnClickListener { select(item) }
//            binding.time.setOnClickListener { select(item) }
//            binding.content.setOnClickListener { select(item) }
//            binding.root.setOnClickListener { select(item) }
//            binding.executePendingBindings()
//        }
//
//        private fun select(item: AnnouncementTable) {
//            startActivity(Intent(this@PromoPage, DetailPromoPage::class.java))
//        }
//    }
}