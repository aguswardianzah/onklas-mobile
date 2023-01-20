package id.onklas.app.pages.klaspay.promo

import android.os.Bundle
import android.text.Html
import id.onklas.app.R
import id.onklas.app.databinding.DetailPromoPageBinding
import id.onklas.app.pages.Privatepage

class DetailPromoPage : Privatepage() {

    private val binding by lazy { DetailPromoPageBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.content.text = Html.fromHtml(content)
    }

    val content by lazy {
        "Dapatkan cashback 1.500 rupiah untuk setiap transaksi pembayaran tagihan<br />\n" +
                "*berlaku untuk tagihan PLN, PDAM, TELKOM INDIHOME serta BPJS<br /><br />\n" +
                "Siswa pintar belajar berwirausaha dengan Onklas, Yuk..... bayar tagihan temenmu, keluargamu serta tetangga sekitar rumahmu melalui onklas agar lebih untung<br /><br />\n" +
                "        <b>Syarat dan ketentuan:</b><br /><br />\n" +
                "        <ol>\n" +
                "            <li>Promo hanya berlaku untuk produk transaksi tagihan PLN, PDAM, TELKOM, INDIHOME serta BPJS di aplikasi Onklas</li><br />\n" +
                "            <li>Promo hanya berlaku untuk user yang terdaftar di aplikasi Onklas</li><br />\n" +
                "            <li>Cashback flat Rp. 1,500 per transaksi</li><br />\n" +
                "            <li>Cashback akan diterima maksimal 1x24jam</li><br />\n" +
                "            <li>Hanya berlaku untuk transaksi menggunakan pembayaran Klaspay</li><br />\n" +
                "            <li>Promo berlaku untuk 1 user/transaksi</li><br />\n" +
                "            <li>Transaksi yang sudah dibayarkan tidak dapat dikembalikan</li><br />\n" +
                "        </ol>"
    }
}