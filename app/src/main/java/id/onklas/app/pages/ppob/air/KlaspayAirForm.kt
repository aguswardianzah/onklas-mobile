package id.onklas.app.pages.ppob.air

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.KlaspayAirFormBinding

class KlaspayAirForm : Fragment() {

    private lateinit var binding: KlaspayAirFormBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = KlaspayAirFormBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editWilayah.setOnClickListener {
            findNavController().navigate(R.id.action_klaspayAirForm_to_klaspayAirWilayah)
        }
        binding.buttonCheck.setOnClickListener {
            startActivity(Intent(requireActivity(), KlaspayAirDetailPage::class.java))
        }
    }
}