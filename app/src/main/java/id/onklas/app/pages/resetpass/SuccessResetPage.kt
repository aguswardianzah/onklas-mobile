package id.onklas.app.pages.resetpass

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.SuccessResetPageBinding
import id.onklas.app.di.component
import kotlinx.coroutines.launch

class SuccessResetPage : Fragment() {

    private lateinit var binding: SuccessResetPageBinding
    private val viewmodel by activityViewModels<ResetPassViewmodel> { component.resetPassVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        SuccessResetPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel
        countDownTimer.start()

        binding.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                val loading = ProgressDialog.show(requireContext(), "", "mengirimkan kode reset")
                val res = viewmodel.resetPass()
                loading.dismiss()
                if (res) {
                    binding.btnLogin.isEnabled = false
                    countDownTimer.start()
                }
            }
        }
    }

    private val countDownTimer by lazy {
        object : CountDownTimer(60 * 3 * 1000, 1000) {
            override fun onFinish() {
                binding.btnLogin.text = "kirim ulang"
                binding.btnLogin.isEnabled = true
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.btnLogin.text = "kirim ulang ${formatMinute(millisUntilFinished / 1000)}"
            }
        }
    }

    private fun formatMinute(secUntilFinish: Long): String {
        val minute = secUntilFinish / 60
        val sec = secUntilFinish % 60
        return "$minute:$sec"
    }
}