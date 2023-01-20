package id.onklas.app.pages.ppob.bpjs

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.BpjsPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.ConfirmPinPage
import id.onklas.app.pages.ppob.PpobCheckoutPage
import kotlinx.coroutines.launch

class BpjsPage : Privatepage() {

    private val binding by lazy { BpjsPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<BpjsViewModel> { component.bpjsVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.lifecycleOwner = this
        binding.viewmodel = viewmodel

        binding.imageContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 982)
            }
        }

        binding.editPeriode.setOnClickListener {
            ListBpjsPage().show(supportFragmentManager, "list-bpjs")
        }

        binding.buttonCheck.setOnClickListener {
            if (binding.editNomor.text.toString()
                    .isNotEmpty() && binding.editPeriode.text.toString().isNotEmpty()
            ) {
                ConfirmPinPage.getPin(this)
            } else
                toast("Mohon isikan nomor VA Bpjs/periode pembayaran terlebih dahulu")
        }

        binding.executePendingBindings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 982 && resultCode == RESULT_OK) {
            data?.data?.let {
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                    // If the cursor returned is valid, get the phone number
                    if (cursor.moveToFirst()) {
                        val numberIndex =
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        var number = cursor.getString(numberIndex)
                        number = number.replace(Regex("\\D"), "")
                        if (number.startsWith("62")) number = number.replaceFirst("62", "0")
                        binding.editNomor.setText(number)
                    }
                }
            }
        } else if (requestCode == ConfirmPinPage.RC && resultCode == RESULT_OK && data != null) {
            lifecycleScope.launch {
                loading(msg = "sedang mencek tagihan Anda")
                val res = viewmodel.inqBpjs(data.getStringExtra("pin"))

                if (res.isNotEmpty())
                    PpobCheckoutPage.openByTrxId(this@BpjsPage, res)

                dismissloading()
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}