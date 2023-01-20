package id.onklas.app.pages.ppob.pulsa

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import id.onklas.app.R
import id.onklas.app.databinding.PulsaPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage
import id.onklas.app.pages.ppob.PpobCheckoutPage
import timber.log.Timber

class PulsaPage : Privatepage() {

    private val binding by lazy { PulsaPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<PulsaViewModel> { component.pulsaVmFactory }
    private val navController by lazy { findNavController(R.id.page_container) }

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

        binding.buttonPrabayar.setOnClickListener {
            setButtonTop(binding.buttonPrabayar)
            navController.navigate(R.id.action_global_klaspayPulsaPrabayar)
        }

        binding.buttonPascabayar.setOnClickListener {
            setButtonTop(binding.buttonPascabayar)
            navController.navigate(R.id.action_global_klaspayPulsaPascabayar)
        }

        binding.imageClearText.setOnClickListener {
            viewmodel.reset()
        }

        binding.imageContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 982)
            }
        }

        binding.editPhone.doAfterTextChanged {
            handle.removeCallbacks(getProvider)
            handle.postDelayed(getProvider, 250)
        }

        viewmodel.errorString.observe(this, Observer(this::toast))

        viewmodel.providerName.observe(this) {
            binding.imageProvider.setImageResource(
                if (it.contains("telkomsel", true)) R.drawable.ic_telkomsel
                else if (it.contains("indosat", true)) R.drawable.ic_indosat
                else if (it.contains("xl", true)) R.drawable.ic_xl
                else if (it.contains("axis", true)) R.drawable.ic_axis
                else if (it.contains("smartfren", true)) R.drawable.ic_smartfren
                else if (it.contains("3", true) || it.contains("three", true)) R.drawable.ic_pulsa_3
                else R.drawable.ic_phone
            )
        }

        binding.executePendingBindings()
    }

    private val handle by lazy { Handler() }
    private val getProvider by lazy {
        Runnable {
            lifecycleScope.launchWhenCreated {
                viewmodel.loadProvider()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val buttons by lazy { listOf(binding.buttonPascabayar, binding.buttonPrabayar) }
    private fun setButtonTop(view: MaterialButton) {
        buttons.forEach {
            when (it) {
                view -> {
                    it.setBackgroundColor(colorPrimary)
                    it.setStrokeColorResource(R.color.colorPrimary)
                    it.setTextColor(Color.WHITE)
                }
                else -> {
                    it.setBackgroundColor(Color.TRANSPARENT)
                    it.setStrokeColorResource(R.color.gray)
                    it.setTextColor(colorGray)
                }
            }
        }
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

                        binding.editPhone.setText(number)
                    }
                }
            }
        } else if (requestCode == PpobCheckoutPage.RC && resultCode == Activity.RESULT_OK) {
            finish()
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}