package id.onklas.app.pages.presensi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import id.onklas.app.databinding.PresensiAbsenItemBinding
import id.onklas.app.databinding.PresensiAbsenPageBinding
import id.onklas.app.di.component

class PresensiAbsenPage : Fragment() {

    private val viewmodel by activityViewModels<PresensiViewModel> { component.presensiVmFactory }
    private lateinit var binding: PresensiAbsenPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        PresensiAbsenPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel
        binding.isStudent = viewmodel.pref.getBoolean("is_student")

        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.getAbsensi()
        }

        viewmodel.loadingAbsensi.observe(
            viewLifecycleOwner,
            Observer(binding.swipeRefresh::setRefreshing)
        )

        binding.rvKelas.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                OrientationHelper.VERTICAL
            )
        )
        binding.rvKelas.adapter = adapter
        viewmodel.listAbsensi.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
        })

        viewmodel.getAbsensi()

        binding.btnAbsen.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), PresensiMasukPage::class.java), 492)
        }

        binding.executePendingBindings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 492 && resultCode == Activity.RESULT_OK) {
            viewmodel.getAbsensi()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private val adapter by lazy {
        object : ListAdapter<AbsensiTable, Viewholder>(
            object : DiffUtil.ItemCallback<AbsensiTable>() {
                override fun areItemsTheSame(
                    oldItem: AbsensiTable,
                    newItem: AbsensiTable
                ): Boolean = oldItem.date == newItem.date

                override fun areContentsTheSame(
                    oldItem: AbsensiTable,
                    newItem: AbsensiTable
                ): Boolean = oldItem == newItem
            }
        ) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                holder.bind(getItem(position))
        }
    }

    private class Viewholder(
        parent: ViewGroup,
        val binding: PresensiAbsenItemBinding = PresensiAbsenItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AbsensiTable) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}