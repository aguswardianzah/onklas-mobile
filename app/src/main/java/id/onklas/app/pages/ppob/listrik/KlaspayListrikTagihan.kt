package id.onklas.app.pages.ppob.listrik

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.onklas.app.databinding.KlaspayListrikTagihanBinding

class KlaspayListrikTagihan : Fragment() {

    private lateinit var binding: KlaspayListrikTagihanBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = KlaspayListrikTagihanBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonNext.setOnClickListener {
            startActivity(Intent(requireActivity(), KlaspayListrikDetailPage::class.java))
        }
    }
}