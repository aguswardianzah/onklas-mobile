package id.onklas.app.pages.entrepreneurs.myMerchant.mystore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import id.onklas.app.GlideApp
import id.onklas.app.databinding.ServicesPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM

class ServicesPage : Fragment() {
    private lateinit var binding: ServicesPageBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return ServicesPageBinding.inflate(inflater, container, false).also { binding=it }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lifecycleOwner = viewLifecycleOwner

    }
}