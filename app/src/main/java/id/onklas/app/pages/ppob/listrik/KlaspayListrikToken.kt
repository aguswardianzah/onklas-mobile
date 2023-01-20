package id.onklas.app.pages.ppob.listrik

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayListrikTokenBinding
import id.onklas.app.databinding.KlaspayListrikTokenItemBinding
import id.onklas.app.di.component
import id.onklas.app.utils.GridSpacingItemDecoration

class KlaspayListrikToken : Fragment() {

    private lateinit var binding: KlaspayListrikTokenBinding
    private val viewModel by activityViewModels<ListrikViewModel> { component.listrikVmFactory }
    private val itemSpace by lazy { resources.getDimensionPixelSize(R.dimen._6sdp) }
    private val menuItemDecoration by lazy {
        GridSpacingItemDecoration(2, itemSpace, false)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = KlaspayListrikTokenBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.listToken.apply {
            adapter = adapterToken
            addItemDecoration(menuItemDecoration)
        }

        binding.buttonNext.setOnClickListener {
            startActivity(Intent(requireActivity(), KlaspayListrikDetailPage::class.java))
        }

    }

    private val listToken by lazy {
        arrayListOf(
                Triple("Token 20.000", "Rp. 22.750", 22750),
                Triple("Token 50.000", "Rp. 52.750", 52750),
                Triple("Token 100.000", "Rp. 102.750", 102750),
                Triple("Token 200.000", "Rp. 202.750", 202750),
                Triple("Token 500.000", "Rp. 502.750", 502750),
                Triple("Token 1.000.000", "Rp. 1.002.750", 1002750),
                Triple("Token 5.000.000", "Rp. 5.002.750", 5002750),
                Triple("Token 10.000.000", "Rp. 10.002.750", 10002750),
        )
    }

    private val adapterToken by lazy {
        object : RecyclerView.Adapter<Viewholder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder =
                    Viewholder(parent)

            override fun onBindViewHolder(holder: Viewholder, position: Int) =
                    holder.bind(listToken[position])

            override fun getItemCount(): Int = listToken.size
        }
    }

    private inner class Viewholder(
            parent: ViewGroup,
            val binding: KlaspayListrikTokenItemBinding = KlaspayListrikTokenItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Triple<String, String, Int>) {
            binding.title = item.first
            binding.price = item.second
            binding.executePendingBindings()
        }
    }
}