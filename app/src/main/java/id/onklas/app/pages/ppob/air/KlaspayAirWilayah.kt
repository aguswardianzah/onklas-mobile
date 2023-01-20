package id.onklas.app.pages.ppob.air

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.onklas.app.databinding.KlaspayAirWilayahBinding

class KlaspayAirWilayah : Fragment() {

    private lateinit var binding: KlaspayAirWilayahBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = KlaspayAirWilayahBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}