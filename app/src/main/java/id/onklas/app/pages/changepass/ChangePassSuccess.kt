package id.onklas.app.pages.changepass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.onklas.app.databinding.ChangePassSuccessBinding

class ChangePassSuccess : Fragment() {

    private lateinit var binding: ChangePassSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        ChangePassSuccessBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnDone.setOnClickListener {
            requireActivity().finish()
        }
    }
}