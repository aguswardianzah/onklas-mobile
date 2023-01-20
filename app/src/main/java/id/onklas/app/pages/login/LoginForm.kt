package id.onklas.app.pages.login

import android.app.ProgressDialog
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
import id.onklas.app.databinding.LoginFormBinding
import id.onklas.app.di.component
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginForm : Fragment() {

    private lateinit var binding: LoginFormBinding
    private val viewmodel by activityViewModels<LoginViewModel> { component.loginVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = LoginFormBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = viewLifecycleOwner

        viewmodel.nisn.observe(viewLifecycleOwner, allowProcessObserver)
        viewmodel.school.observe(viewLifecycleOwner, allowProcessObserver)
        viewmodel.allowProcesslogin.observe(viewLifecycleOwner, Observer {
            binding.btnLogin.isEnabled = it
        })

        binding.inSekolah.setOnClickListener {
            ListSekolahPage {
                viewmodel.school.postValue(it)
            }.show(childFragmentManager, "")
        }

        binding.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                val progress = ProgressDialog.show(
                    requireActivity(),
                    "Mohon Tunggu",
                    "Sedang mencari data pengguna..."
                )
                val (success, message) = viewmodel.getUserData()
                Timber.e("success: $success -- message: $message")
                if (success)
                    findNavController().navigate(R.id.action_loginForm_to_loginProcess)
                else
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        }
    }

    private val allowProcessObserver by lazy {
        Observer<Any> {
            viewmodel.allowProcesslogin.postValue(!viewmodel.nisn.value.isNullOrBlank() && viewmodel.school.value != null)
        }
    }
}