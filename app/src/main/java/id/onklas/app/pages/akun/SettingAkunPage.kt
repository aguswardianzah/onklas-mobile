package id.onklas.app.pages.akun

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.DialogEmailSentBinding
import id.onklas.app.databinding.SettingAkunPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import kotlinx.coroutines.launch

class SettingAkunPage : Privatepage() {

    private val binding by lazy { SettingAkunPageBinding.inflate(layoutInflater) }
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
        binding.lifecycleOwner = this

        binding.btnEditEmail.setOnClickListener {
            if (binding.btnEditEmail.text == "batal")
                viewmodel.email.postValue(viewmodel.userTable.email)
            viewmodel.editEmail.postValue(viewmodel.editEmail.value?.not())
        }

        viewmodel.editEmail.observe(this, {
            setButton(it)
            if (viewmodel.isVerifying.value!!) {
                if (it) {
                    binding.btnVerify.visibility = View.VISIBLE
                    binding.labelVerifying.visibility = View.GONE
                } else {
                    binding.btnVerify.visibility = View.GONE
                    binding.labelVerifying.visibility = View.VISIBLE
                }
            }
        })

        binding.btnVerify.setOnClickListener {
            lifecycleScope.launch {
                val loading = ProgressDialog.show(this@SettingAkunPage, "", "Mohon tunggu")
                viewmodel.updateProfile {
                    if (it) {
                        lifecycleScope.launch {
                            val result = viewmodel.sendEmail()
                            loading.dismiss()
                            if (result) showDialogEmailSent()
                        }
                    } else loading.dismiss()
                }
            }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))

        viewmodel.isEmailVerified.observe(this, {
            if (it) {
                viewmodel.pref.putBoolean("is_email_verifying", false)
                viewmodel.isVerifying.postValue(false)
            }
        })

        binding.inEmail.doAfterTextChanged { setButton(it!!.isNotEmpty()) }

        binding.inUsername.addTextChangedListener(inputWatcher)
        binding.inName.addTextChangedListener(inputWatcher)
        binding.inEmail.addTextChangedListener(inputWatcher)
        binding.inPhone.addTextChangedListener(inputWatcher)

        viewmodel.hasChange.observe(this) { invalidateOptionsMenu() }

        binding.executePendingBindings()
    }

    private fun setButton(enable: Boolean) {
        if (enable) {
            binding.btnVerify.isEnabled = true
            binding.btnVerify.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.btnVerify.setStrokeColorResource(R.color.colorPrimary)
        } else {
            binding.btnVerify.isEnabled = false
            binding.btnVerify.setTextColor(ContextCompat.getColor(this, R.color.gray))
            binding.btnVerify.setStrokeColorResource(R.color.gray)
        }
    }

    private val inputWatcher by lazy {
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                allowUpdate()

                binding.labelVerified.visibility =
                    if (viewmodel.emailChanged()) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun allowUpdate(): Boolean {
        binding.nameAlert.visibility =
            if (binding.inName.text.toString().isEmpty()) View.VISIBLE else View.GONE
        binding.emailAlert.visibility =
            if (binding.inEmail.text.toString().isEmpty()) View.VISIBLE else View.GONE

        return viewmodel.hasChange().also { viewmodel.hasChange.postValue(it) }
    }

    private fun showDialogEmailSent() {
        val dialogBinding = DialogEmailSentBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_save)?.isEnabled = viewmodel.hasChange()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_setting_akun, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Timber.e("allowUpdate ${allowUpdate()}")
        if (item.itemId == R.id.menu_save) {
            lifecycleScope.launch {
                loading(msg = "Sedang mengupdate data")
                val updateRes = viewmodel.updateProfile()
                if (updateRes) {
                    toast("Data berhasil diupdate")
                    if (viewmodel.emailChanged() && viewmodel.sendEmail()) {
                        dismissloading()
                        showDialogEmailSent()
                    } else {
                        setResult(
                            RESULT_OK,
                            Intent().putExtra("username", binding.inUsername.text.toString())
                        )
                        finish()
                    }
                } else {
                    toast("Gagal mengupdate data, mohon ulangi beberapa saat lagi")
                }
                dismissloading()
            }
        }
        return true
    }
}