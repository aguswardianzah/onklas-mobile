package id.onklas.app.pages.pembayaran

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.ChangePasswordSuccessDialogBinding
import id.onklas.app.databinding.ConfirmPinPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.home.dialogs.HomeDialog
import id.onklas.app.pages.klaspay.riwayat.KlaspayRiwayatPage
import id.onklas.app.utils.showKeyboard
import kotlinx.coroutines.launch

class ConfirmPinPage : Privatepage() {

    private val viewmodel by viewModels<ConfirmPinViewModel> { component.klaspayConfirmPinVmFactory }
    private val binding by lazy { ConfirmPinPageBinding.inflate(layoutInflater) }
    private val type by lazy { intent.getStringExtra("type") ?: "SPP" }
    private val channelKlaspay by lazy { intent.getStringExtra("channel_klaspay") ?: "KLASPAY" }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        binding.viewmodel = viewmodel
        binding.lifecycleOwner = this
        binding.type = this.type

        binding.btnForgotPin.setOnClickListener {
            if (!viewmodel.pref.getBoolean("is_email_verified") || viewmodel.userTable.email.isEmpty())
                HomeDialog.showEmailSettingDialog(supportFragmentManager)
            else
                lifecycleScope.launch {
                    loading(msg = "sedang memproses")
                    val res = viewmodel.resetPin()
                    dismissloading()

                    if (res?.email_verified == true) {
                        startActivity(
                            Intent(
                                this@ConfirmPinPage,
                                SetPinKlaspayPage::class.java
                            ).putExtra("token", res.reset_token)
                        )
                    } else {
                        val dialogBinding =
                            ChangePasswordSuccessDialogBinding.inflate(layoutInflater).apply {
                                image.setImageResource(R.drawable.img_reset_pin)
                                title.text = "Konfirmasi Pergantian Pin Terkirim"
                                msg.text =
                                    "Konfirmasi pergantian pin telah kami kirim ke email kamu yang terdaftar di Onklas. Periksa email tersebut untuk mendapat pin baru"
                                btnVerify.text = "Ok, Terima Kasih"
                            }
                        val dialog = AlertDialog.Builder(this@ConfirmPinPage)
                            .setView(dialogBinding.root)
                            .show()
                            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                        dialogBinding.btnVerify.setOnClickListener {
                            viewmodel.intentUtil.openEmail(this@ConfirmPinPage) { dialog.dismiss() }
                        }
                    }
                }
        }


        binding.btnCheckout.setOnClickListener {
            lifecycleScope.launch {
                if (type == "get_pin") {
                    setResult(RESULT_OK, Intent().putExtra("pin", viewmodel.pin.value))
                    finish()
                } else if (type == "SPP") {
                    loading(msg = "Memproses pembayaran")
                    if (intent.getBooleanExtra("use_klaspay", false)) {
                        val keys = intent.getStringArrayExtra("keys")
                        val successIndex = ArrayList<Int>()

                        var index = 0
                        for (s in intent.getStringArrayExtra("others") ?: emptyArray()) {
                            keys?.get(index)?.let {
                                updateLoading(msg = "Memproses pembayaran $it")
                            }
                            if (!viewmodel.paySpp(s, channelKlaspay))
                                break
                            else
                                successIndex.add(index)

                            index++
                        }

//                    intent.getStringArrayExtra("others")?.forEachIndexed { index, s ->
//                        keys?.get(index)?.let {
//                            updateLoading(msg = "Memproses pembayaran $it")
//                        }
//                        if (!viewmodel.paySpp(s))
//                            return
//                        else
//                            successIndex.add(index)
//                    }

                        if (successIndex.isNotEmpty()) {
                            SuccessPayPage.open(
                                this@ConfirmPinPage,
                                intent.getStringExtra("type").orEmpty(),
                                intent.getStringExtra("total").orEmpty(),
                                intent.getStringArrayExtra("keys")
                                    ?.filterIndexed { index, s -> successIndex.contains(index) }
                                    ?.toTypedArray()
                                    ?: emptyArray()
                            )

                            setResult(RESULT_OK)
                            finish()
                        }
                    } else {
                        lifecycleScope.launch {
                            val loading =
                                ProgressDialog.show(this@ConfirmPinPage, "", "mohon tunggu")
                            val ids =
                                (intent.getStringArrayExtra("others") ?: emptyArray()).joinToString(
                                    ","
                                )
                            viewmodel.paySppChannel(
                                ids,
                                intent.getStringExtra("payment_code").orEmpty()
                            )
                                ?.let {
                                    PaymentGuidePage.open(
                                        this@ConfirmPinPage,
                                        it.data.transaction_id
                                    )
//                                    startActivity(
//                                        Intent(this@ConfirmPinPage, KlaspayBayarPage::class.java)
//                                            .putExtra("from", "topup")
//                                            .putExtra("topup", it)
//                                    )
                                    setResult(RESULT_OK)
                                    finish()
                                }
                            loading.dismiss()
                        }
                    }
                    dismissloading()
                }
            }
        }

        binding.pin.requestFocus()
        binding.executePendingBindings()

        viewmodel.errorString.observe(this, Observer(this::toast))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SuccessPayPage.RC && resultCode == RESULT_OK) {
            if (data?.getBooleanExtra("detail", false) == true) {
                startActivity(Intent(this, KlaspayRiwayatPage::class.java))
            }

            setResult(RESULT_OK)
            finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        const val RC = 4281
        fun getPin(activity: Activity) {
            activity.startActivityForResult(
                Intent(
                    activity,
                    ConfirmPinPage::class.java
                ).putExtra("type", "get_pin"), RC
            )
        }

        fun getPin(fragment: Fragment) {
            fragment.startActivityForResult(
                Intent(
                    fragment.requireContext(),
                    ConfirmPinPage::class.java
                ).putExtra("type", "get_pin"), RC
            )
        }
    }
}