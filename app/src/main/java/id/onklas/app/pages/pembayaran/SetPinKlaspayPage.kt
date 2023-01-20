package id.onklas.app.pages.pembayaran

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.SetPinKlaspayPageBinding
import id.onklas.app.databinding.SetPinSuccessDialogBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class SetPinKlaspayPage : Privatepage() {

    private val binding by lazy { SetPinKlaspayPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ConfirmPinViewModel> { component.klaspayConfirmPinVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.viewmodel = viewmodel

        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.btnSave.setOnClickListener {
            if (viewmodel.setPin.value?.length ?: 0 < 6)
                toast("pin harus berisi 6 digit angka")
            else if (viewmodel.setPin.value != viewmodel.confPin.value)
                toast("pin baru dan konfirmasi tidak sesuai")
            else
                lifecycleScope.launchWhenCreated {
                    loading(msg = "menyimpan perubahan")
                    val res = viewmodel.setPin(intent.getStringExtra("token"))
                    dismissloading()

                    if (res) {
                        val dialogBinding = SetPinSuccessDialogBinding.inflate(layoutInflater)
                        val dialog = AlertDialog.Builder(this@SetPinKlaspayPage)
                            .setView(dialogBinding.root)
                            .show()
                            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                        dialogBinding.btnVerify.setOnClickListener {
                            dialog.dismiss()
                            finish()
                        }
                    }
                }
        }
    }
}