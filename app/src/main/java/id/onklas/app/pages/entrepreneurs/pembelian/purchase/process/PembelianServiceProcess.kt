package id.onklas.app.pages.entrepreneurs.pembelian.purchase.process

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import id.onklas.app.GlideApp
import id.onklas.app.databinding.PembelianServiceProcessBinding
import id.onklas.app.di.component
import id.onklas.app.pages.entrepreneurs.PembelianVM


class PembelianServiceProcess : Fragment() {

    private lateinit var binding: PembelianServiceProcessBinding
    private val viewmodel by activityViewModels<PembelianVM> { component.entrepreneursPembelianFactory }
    private val glide by lazy { GlideApp.with(requireActivity()) }

    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = PembelianServiceProcessBinding.inflate(inflater, container, false)
        .also { binding = it }.root


}