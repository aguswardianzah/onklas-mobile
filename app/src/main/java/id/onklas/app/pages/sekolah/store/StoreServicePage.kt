package id.onklas.app.pages.sekolah.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import id.onklas.app.GlideApp
import id.onklas.app.R
import id.onklas.app.databinding.StoreServicePageBinding
import id.onklas.app.di.component

class StoreServicePage : Fragment() {


    private lateinit var binding: StoreServicePageBinding
    private val viewmodel by activityViewModels<StoreVm> { component.storeVmFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = StoreServicePageBinding.inflate(inflater, container, false)
            .also { binding = it }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.test.setOnClickListener {
            findNavController().navigate(R.id.action_global_storeProductPage)
        }
    }
}