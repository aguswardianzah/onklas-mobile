package id.onklas.app.pages.akun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.DeviceItemBinding
import id.onklas.app.databinding.DevicesPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.login.SessionData
import id.onklas.app.utils.LinearSpaceDecoration
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch

class DevicesPage : Privatepage() {

    val binding by lazy { DevicesPageBinding.inflate(layoutInflater) }
    val viewmodel by viewModels<DeviceViewModel> { component.deviceViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewmodel.loading.observe(this, Observer(binding.swipeRefresh::setRefreshing))
        viewmodel.errorString.observe(this, Observer(this::toast))
        viewmodel.listDevice.observe(this, Observer(adapter::submitList))

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch { viewmodel.refresh() }
        }

        binding.rvDevice.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(
                    R.dimen._8sdp
                ),
                includeTop = true, includeBottom = true
            )
        )
        binding.rvDevice.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_device, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AlertDialog.Builder(this@DevicesPage, R.style.DialogTheme)
            .setMessage("Anda yakin akan keluar dari semua device yang lain?")
            .setPositiveButton("Logout") { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch {
                    loading(msg = "Mohon tunggu")
                    viewmodel.deleteAllSession()
                    dismissloading()
                }
            }
            .setNeutralButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()

        return super.onOptionsItemSelected(item)
    }

    private val adapter by lazy {
        object :
            PagingAdapter<SessionData, Viewholder>(object : DiffUtil.ItemCallback<SessionData>() {
                override fun areItemsTheSame(oldItem: SessionData, newItem: SessionData): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: SessionData,
                    newItem: SessionData
                ): Boolean =
                    oldItem == newItem
            }) {

            override fun createItemViewholder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun bindItemViewholder(holder: Viewholder, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(
                holder: Viewholder,
                position: Int,
                payloads: MutableList<Any>
            ) = bindItemViewholder(holder, position)
        }
    }

    inner class Viewholder(
        parent: ViewGroup, val binding: DeviceItemBinding = DeviceItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionData) {
            binding.item = item
            binding.btnLogout.setOnClickListener {
                AlertDialog.Builder(this@DevicesPage, R.style.DialogTheme)
                    .setMessage("Anda yakin akan keluar dari device?")
                    .setPositiveButton("Logout") { dialog, _ ->
                        dialog.dismiss()
                        lifecycleScope.launch {
                            this@DevicesPage.loading(msg = "Mohon tunggu")
                            viewmodel.deleteSession(item.id)
                            this@DevicesPage.dismissloading()
                        }
                    }
                    .setNeutralButton("Batal") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            binding.executePendingBindings()
        }
    }
}