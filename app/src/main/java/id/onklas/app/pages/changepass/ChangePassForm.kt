package id.onklas.app.pages.changepass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.ChangePassFormBinding
import id.onklas.app.di.component
import kotlinx.coroutines.launch

class ChangePassForm : Fragment() {

    private lateinit var binding: ChangePassFormBinding
    private val viewmodel by activityViewModels<ChangePassViewmodel> { component.changePassVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ChangePassFormBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel
        viewmodel.oldPass.observe(viewLifecycleOwner, checkPassObs)
        viewmodel.newPass.observe(viewLifecycleOwner, checkPassObs)
        binding.btnChangePass.setOnClickListener {
            if (viewmodel.newPass.value != viewmodel.confirmPass.value) {
                Toast.makeText(requireContext(), "Password baru tidak sesuai", Toast.LENGTH_SHORT)
                    .show()
            } else
                lifecycleScope.launch {
                    viewmodel.changePass {
                        if (it)
                            findNavController().navigate(R.id.action_changePassForm_to_changePassSuccess)
                    }
                }
        }
    }

    private val checkPassObs by lazy {
        Observer<Any> {
            binding.btnChangePass.isEnabled = (
                    viewmodel.oldPass.value.orEmpty().isNotEmpty() &&
                            viewmodel.newPass.value.orEmpty().isNotEmpty()
                    )
        }
    }
}