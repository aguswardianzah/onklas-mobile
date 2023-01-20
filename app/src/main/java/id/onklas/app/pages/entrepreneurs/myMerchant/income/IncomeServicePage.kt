package id.onklas.app.pages.entrepreneurs.myMerchant.income

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import id.onklas.app.GlideApp
import id.onklas.app.databinding.IncomeServicePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.EntrepreneursVM


class IncomeServicePage : Fragment() {

    private lateinit var binding: IncomeServicePageBinding
    private val viewmodel by activityViewModels<EntrepreneursVM> { component.entrepreneursFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = IncomeServicePageBinding.inflate(inflater, container, false).also { binding = it}.root

}