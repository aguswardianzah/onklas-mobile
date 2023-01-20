package id.onklas.app.pages.klaspay.aktivasi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayAktivasiPinBinding
import id.onklas.app.di.component

class KlaspayAktivasiPin : Fragment() {

    private lateinit var binding: KlaspayAktivasiPinBinding
    private val viewModel by activityViewModels<KlaspayAktivasiViewmodel> { component.klaspayAktivasiVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = KlaspayAktivasiPinBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pageStep.postValue(2)
        binding.labelTitle.text = "BUAT 6 DIGIT PIN KEAMANAN"
        binding.labelDescription.text = "Akan diminta setiap Anda akan melakukan transaksi pembayaran"
        binding.labelError.visibility = View.GONE

        binding.buttonConfirm.setOnClickListener {
            findNavController().navigate(R.id.action_KlaspayAktivasiPin_to_klaspayAktivasiPinConfirm)
        }

        binding.pinField.doAfterTextChanged {
            viewModel.pin.postValue(it.toString())
        }

        viewModel.pin.observe(viewLifecycleOwner, {
             binding.buttonConfirm.isEnabled = (it.length == 6)
        })

    }

}