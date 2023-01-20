package id.onklas.app.pages.ujian

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.ListItemBinding
import id.onklas.app.databinding.PrepareUjianPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.utils.LinearSpaceDecoration
import kotlinx.coroutines.launch

class PrepareUjianPage : Privatepage() {

    private val binding by lazy { PrepareUjianPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<UjianViewModel> { component.ujianVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener {
                if (viewmodel.isDownloadingSoal.value == true)
                    toast("Sedang proses mendownload ujian")
                else
                    finish()
            }
        }

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = this
        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.rvRules.adapter = adapter
        binding.rvRules.addItemDecoration(LinearSpaceDecoration(includeTop = true))

        adapter.submitList(
            listOf(
                "Dilarang menutup/meninggalkan aplikasi saat ujian berlangsung. Jika aplikasi ditutup/ditinggalkan selama proses ujian, maka peserta dianggap telah menyelesaikan ujian.",
                "Ketika proses ujian sedang berlangsung, pastikan kamu tidak mematikan layar handphone kamu dengan menekan tombol power karena akan membuat ujian otomatis selesai.",
                "Kerjakan soal yang menurut Anda lebih mudah terlebih dahulu, Anda dapat mengubah halaman soal pada menu di sebelah kanan atas.",
                "Selamat mengerjakan, semoga berhasil!"
            )
        )

        binding.btnStart.setOnClickListener {
            lifecycleScope.launch {
                val progress = ProgressDialog.show(this@PrepareUjianPage, "", "memulai ujian")
                val success = viewmodel.startExam(
                    intent.getStringExtra("id").orEmpty(),
                    binding.inPassword.text.toString()
                )
                progress.dismiss()
                if (success) {
                    startActivity(
                        Intent(
                            this@PrepareUjianPage,
                            TakeUjianPage::class.java
                        ).putExtras(intent)
                    )
                    finish()
                }
            }
        }

        binding.btnDownload.setOnClickListener {
            lifecycleScope.launch {
                viewmodel.downloadSoal(intent.getStringExtra("id").orEmpty())
            }
        }

        lifecycleScope.launchWhenCreated {
            if (intent.hasExtra("id")) {
                viewmodel.downloadSoal(intent.getStringExtra("id").orEmpty())
            } else {
                AlertDialog.Builder(this@PrepareUjianPage, R.style.DialogTheme)
                    .setTitle("Perhatian")
                    .setMessage("Ujian tidak valid")
                    .setPositiveButton("Tutup") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
            }
        }
    }

    override fun onBackPressed() {
        if (viewmodel.isDownloadingSoal.value == true)
            toast("Sedang proses mendownload ujian")
        else
            super.onBackPressed()
    }

    private val adapter by lazy {
        object : ListAdapter<String, Viewholder>(object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private class Viewholder(
        parent: ViewGroup,
        val binding: ListItemBinding = ListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(content: String) {
            binding.content.text = content
        }
    }
}