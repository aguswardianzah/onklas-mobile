package id.onklas.app.pages.sekolah.store

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.CheckoutStoreAddressPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class CheckoutStoreAddressPage : Privatepage() {

    private val binding by lazy { CheckoutStoreAddressPageBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<CheckoutAddressViewModel> { component.checkoutAddresVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        binding.inProvince.setOnClickListener { dialogSelect() }
        binding.actionProvince.setOnClickListener { dialogSelect() }
        binding.provinceLabel.setOnClickListener { dialogSelect() }

        binding.inCity.setOnClickListener { dialogSelect(1) }
        binding.actionCity.setOnClickListener { dialogSelect(1) }
        binding.cityLabel.setOnClickListener { dialogSelect(1) }

        binding.inDistrict.setOnClickListener { dialogSelect(2) }
        binding.actionDistrict.setOnClickListener { dialogSelect(2) }
        binding.districtLabel.setOnClickListener { dialogSelect(2) }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launchWhenCreated {
                loading(msg = "menyimpan data alamat")
                val res = viewModel.saveAddress()
                dismissloading()

                if (res) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }

        binding.executePendingBindings()
    }

    private fun dialogSelect(type: Int = 0) {
        when (type) {
            1 -> {
                viewModel.selectedProvince.value?.let {
                    CheckoutStoreLocDialog(
                        City::class.java,
                        viewModel.listCity(it.id),
                        { viewModel.selectedCity.postValue(it) },
                        it.name
                    ).show(supportFragmentManager, "select_province")
                }
            }
            2 -> {
                viewModel.selectedCity.value?.let {
                    CheckoutStoreLocDialog(
                        District::class.java,
                        viewModel.listDistrict(it.id),
                        { viewModel.selectedDistrict.postValue(it) },
                        it.name
                    ).show(supportFragmentManager, "select_city")
                }
            }
            else -> {
                CheckoutStoreLocDialog(
                    Province::class.java,
                    viewModel.listProvince,
                    { viewModel.selectedProvince.postValue(it) }
                ).show(supportFragmentManager, "select_district")
            }
        }
    }
}