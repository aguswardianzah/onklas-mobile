package id.onklas.app.pages.akun

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.DialogEmailSentBinding
import id.onklas.app.databinding.SettingContactPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingContactPage : Privatepage() {

    private val binding by lazy { SettingContactPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<SettingAkunViewmodel> { component.settingAkunVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.viewmodel = viewmodel

        binding.btnEditEmail.setOnClickListener { viewmodel.editEmail.postValue(viewmodel.editEmail.value?.not()) }
        binding.btnEditPhone.setOnClickListener { viewmodel.editPhone.postValue(viewmodel.editPhone.value?.not()) }

        binding.btnVerify.setOnClickListener {
            lifecycleScope.launch {
                val loading = ProgressDialog.show(this@SettingContactPage, "", "Mohon tunggu")
                val result = viewmodel.sendEmail()
                loading.dismiss()
                if (result) showDialogEmailSent()
            }
        }

//        binding.inUsername.addTextChangedListener(inputWatcher)
        binding.inPhone.addTextChangedListener(inputWatcher)
        binding.inEmail.addTextChangedListener(inputWatcher)
        binding.btnUpdate.setOnClickListener {
            lifecycleScope.launch {
                loading(msg = "Sedang mengupdate data")
                viewmodel.updateProfile {
                    if (it) {
                        toast("Data berhasil diupdate")
                        setResult(
                            RESULT_OK,
                            Intent().putExtra("username", binding.inUsername.text.toString())
                        )
                        finish()
                    } else {
                        toast("Gagal mengupdate data, mohon ulangi beberapa saat lagi")
                    }
                }
                dismissloading()
            }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))

        checkAllowUpdate()

        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }

    private fun showDialogEmailSent() {
        val dialogBinding = DialogEmailSentBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).show()
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
    }

    private val inputWatcher by lazy {
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewmodel.isVerified.postValue(
                    binding.inEmail.text.toString() == viewmodel.userTable.email && viewmodel.pref.getBoolean(
                        "is_verified"
                    )
                )
                checkAllowUpdate()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun checkAllowUpdate() {
        binding.btnUpdate.isEnabled = (
//                binding.inUsername.text.toString().isNotEmpty() &&
                binding.inEmail.text.toString().isNotEmpty() &&
                        binding.inPhone.text.toString().isNotEmpty()
                )
    }
}