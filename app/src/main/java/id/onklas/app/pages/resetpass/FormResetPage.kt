package id.onklas.app.pages.resetpass

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.FormResetPageBinding
import id.onklas.app.di.component

class FormResetPage : Fragment() {

    private lateinit var binding: FormResetPageBinding
    private val viewmodel by activityViewModels<ResetPassViewmodel> { component.resetPassVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FormResetPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel
        binding.inEmail.doAfterTextChanged {
            binding.btnLogin.isEnabled = it.toString().length > 3
        }

        binding.btnLogin.setOnClickListener {
            lifecycleScope.launchWhenCreated {
                val loading = ProgressDialog.show(requireContext(), "", "mengirimkan kode reset")
                val res = viewmodel.resetPass()
                loading.dismiss()
                if (res)
                    findNavController().navigate(
                        R.id.action_formResetPage_to_successResetPage,
                        Bundle().apply { putString("emailArg", binding.inEmail.text.toString()) })
            }
        }
    }
}