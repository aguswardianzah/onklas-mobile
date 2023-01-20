package id.onklas.app.pages.ppob.internet

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.InternetPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.pembayaran.ConfirmPinPage
import id.onklas.app.pages.ppob.PpobCheckoutPage
import kotlinx.coroutines.launch

class InternetPage : Privatepage() {

    private val binding by lazy { InternetPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<InternetViewModel> { component.internetVmFactory }

    init {
        lifecycleScope.launchWhenCreated {
            try {
                viewmodel.loadJenis()
            } catch (e: Exception) {
            }
        }
    }

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

        viewmodel.errorString.observe(this, Observer(this::toast))

        binding.editJenis.setOnClickListener {
            ListInternetPage().show(
                supportFragmentManager,
                "list-internet"
            )
        }

        binding.imageContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 982)
            }
        }

        binding.buttonCheck.setOnClickListener {
            if (binding.editNomor.text.toString()
                    .isNotEmpty() && binding.editJenis.text.toString().isNotEmpty()
            ) {
                ConfirmPinPage.getPin(this)
            } else
                toast("Mohon isikan nomor pelanggan/penyedia layanan terlebih dahulu")
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
                        val number = cursor.getString(numberIndex).apply {
                            if (this.startsWith("+")) substring(1)
                            if (this.startsWith("62")) replaceFirst("62", "0")
                        }
                        binding.editNomor.setText(number)
                    }
                }
            }
        } else if (requestCode == ConfirmPinPage.RC && resultCode == RESULT_OK && data != null) {
            lifecycleScope.launch {
                loading(msg = "sedang mencek tagihan Anda")
                val res = viewmodel.inqInternet(data.getStringExtra("pin"))

                if (res.isNotEmpty())
                    PpobCheckoutPage.openByTrxId(this@InternetPage, res)

                dismissloading()
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}