package id.onklas.app.pages.pembayaran

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import id.onklas.app.R
import id.onklas.app.databinding.CashbackKlaspayDialogBinding
import id.onklas.app.databinding.ComingSoonDialogBinding
import id.onklas.app.databinding.DialogActivateKlaspayBinding
import id.onklas.app.databinding.PembayaranPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.pages.home.dialogs.HomeDialog
import id.onklas.app.pages.klaspay.aktivasi.KlaspayAktivasiPage
import id.onklas.app.pages.klaspay.aktivasi.KlaspayAktivasiViewmodel
import id.onklas.app.pages.klaspay.promo.DetailPromoPage
import id.onklas.app.pages.klaspay.promo.PromoPage
import id.onklas.app.pages.klaspay.riwayat.KlaspayRiwayatPage
import id.onklas.app.pages.klaspay.tagihan.KlaspayTagihanPage
import id.onklas.app.pages.klaspay.topup.KlaspayTopupPage
import id.onklas.app.pages.partisipasi.PartisipasiPage
import id.onklas.app.pages.pembayaran.spp.SppPaymentPage
import id.onklas.app.pages.perpus.PerpusPage
import id.onklas.app.pages.ppob.air.AirPage
import id.onklas.app.pages.ppob.bpjs.BpjsPage
import id.onklas.app.pages.ppob.game.GamePage
import id.onklas.app.pages.ppob.internet.InternetPage
import id.onklas.app.pages.ppob.listrik.ListrikPage
import id.onklas.app.pages.ppob.pulsa.PulsaPage
import id.onklas.app.utils.GridSpacingItemDecoration
import id.onklas.app.utils.TopSheetBehavior
import kotlinx.coroutines.launch

class PembayaranPage : BaseFragment() {

    private lateinit var binding: PembayaranPageBinding
    private val viewmodel by activityViewModels<PaymentViewModel> { component.paymentVmFactory }
    private val klaspayActivasiVm by activityViewModels<KlaspayAktivasiViewmodel> { component.klaspayAktivasiVmFactory }

    private val itemSpace by lazy { resources.getDimensionPixelSize(R.dimen._8sdp) }
    private val edgeSpace by lazy { resources.getDimensionPixelSize(R.dimen._16sdp) }
    private val menuItemDecoration by lazy {
        GridSpacingItemDecoration(3, itemSpace, false)
    }

    private val topSheet by lazy { TopSheetBehavior.from(binding.dialogInfo.root) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = PembayaranPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        binding.dialogInfo.lifecycleOwner = viewLifecycleOwner
        binding.dialogInfo.viewmodel = viewmodel
        binding.dialogInfo.btnClose.setOnClickListener {
            topSheet.state = TopSheetBehavior.STATE_COLLAPSED
        }
        binding.dialogInfo.btnCopy.setOnClickListener {
            viewmodel.intentUtil.copyText(
                requireActivity(),
                binding.dialogInfo.textId.text.toString()
            )
            toast("ID Klaspay tersalin")
        }

        binding.icKlaspay.setOnClickListener { showDialogInfo() }
        binding.btnInfo.setOnClickListener { showDialogInfo() }

        binding.btnAktivasi.setOnClickListener {
            if (!viewmodel.pref.getBoolean("is_email_verified"))
                HomeDialog.showEmailSettingDialog(childFragmentManager) {
                    if (viewmodel.pref.getBoolean("default_pass"))
                        HomeDialog.showChangePasswordDialog(
                            requireContext(),
                            childFragmentManager,
                            layoutInflater
                        )
                    else binding.btnAktivasi.performClick()
                }
            else if (viewmodel.pref.getBoolean("default_pass"))
                HomeDialog.showChangePasswordDialog(
                    requireContext(),
                    childFragmentManager,
                    layoutInflater
                ) {
                    binding.btnAktivasi.performClick()
                }
            else
                lifecycleScope.launch {
                    val loading = ProgressDialog.show(requireContext(), "", "Memverifikasi data")
                    klaspayActivasiVm.klaspayCheck { success, errorTypes ->
                        loading.dismiss()
                        if (success)
                            startActivity(
                                Intent(requireActivity(), KlaspayAktivasiPage::class.java)
                            )
                        else if (errorTypes.contains("email"))
                            HomeDialog.showEmailSettingDialog(childFragmentManager) {
                                if (viewmodel.pref.getBoolean("default_pass"))
                                    HomeDialog.showChangePasswordDialog(
                                        requireContext(),
                                        childFragmentManager,
                                        layoutInflater
                                    )
                                else binding.btnAktivasi.performClick()
                            }
                        else if (errorTypes.contains("password"))
                            HomeDialog.showChangePasswordDialog(
                                requireContext(),
                                childFragmentManager,
                                layoutInflater
                            ) {
                                binding.btnAktivasi.performClick()
                            }
                    }
                }
        }

        binding.icTopup.setOnClickListener {
            if (viewmodel.isKlaspayActive.value == true)
                startActivity(
                    Intent(requireActivity(), KlaspayTopupPage::class.java)
                )
            else
                showDialogActivate()
        }

        binding.icHistory.setOnClickListener {
            if (viewmodel.isKlaspayActive.value == true)
                startActivity(
                    Intent(requireActivity(), KlaspayRiwayatPage::class.java)
                )
            else
                showDialogActivate()
        }

        binding.cardTagihan.setOnClickListener {
            if (viewmodel.isKlaspayActive.value == true)
                startActivity(
                    Intent(requireActivity(), KlaspayTagihanPage::class.java)
                )
            else
                showDialogActivate()
        }

        binding.icTransfer.setOnClickListener { showDialogComingSoon() }

        binding.rvMenu.apply {
            adapter = PaymentItemAdapter(listBayar, onClick)
            addItemDecoration(menuItemDecoration)
        }
        binding.executePendingBindings()

        binding.rvBayar.apply {
            adapter = PaymentItemAdapter(
                listOf(
                    Triple(R.drawable.ic_pulsa, "Pulsa", false),
                    Triple(R.drawable.ic_bayar_listrik, "Listrik", false),
                    Triple(R.drawable.ic_bayar_air, "Air", false),
                    Triple(R.drawable.ic_internet, "Internet", false),
                    Triple(R.drawable.ic_voucher_game, "Game", false),
                    Triple(R.drawable.ic_bpjs, "BPJS", false)
                ), ppobClickHandler
            )
            addItemDecoration(menuItemDecoration)
        }

        viewmodel.isKlaspayActive.observe(viewLifecycleOwner) {
            if (it && !viewmodel.pref.getBoolean("show_klaspay_cashback")) {
                val binding = CashbackKlaspayDialogBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(binding.root)
                    .show()
                    .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

                binding.root.setOnClickListener {
                    dialog.dismiss()
                    startActivity(Intent(requireContext(), DetailPromoPage::class.java))
                }

                viewmodel.pref.putBoolean("show_klaspay_cashback", true)
            }
        }

        binding.promo.setOnClickListener {
            startActivity(
                Intent(requireContext(), PromoPage::class.java)
            )
        }

        binding.imgCashbackBanner.setOnClickListener {
            startActivity(
                Intent(requireContext(), DetailPromoPage::class.java)
            )
        }

//
//        binding.icScan.setOnClickListener { showDialogComingSoon() }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launchWhenCreated {
            viewmodel.getWallet()
            binding.executePendingBindings()
        }
    }

    private fun showDialogInfo() {
        if (viewmodel.isKlaspayActive.value == true)
            topSheet.state = TopSheetBehavior.STATE_EXPANDED
        else
            showDialogActivate()
    }

    private fun showDialogActivate() {
        val dialogBinding = DialogActivateKlaspayBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .show()
            .apply { window?.setBackgroundDrawableResource(R.drawable.rounded_white) }

        dialogBinding.btnActivate.setOnClickListener {
            startActivity(Intent(requireActivity(), KlaspayAktivasiPage::class.java))
            dialog.dismiss()
        }

        dialogBinding.btnLater.setOnClickListener { dialog.dismiss() }
    }

    private fun showDialogComingSoon() {
        val dialogBinding = ComingSoonDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_white)
        dialogBinding.btnOk.setOnClickListener { dialog.dismiss() }
    }

    private val isStudent by lazy { viewmodel.pref.getBoolean("is_student") }
    private val listBayar by lazy {
        arrayListOf(
            Triple(R.drawable.ic_pay_spp, "Bayar Spp", false),
            Triple(R.drawable.ic_dana_partisipasi, "Dana Partisipasi", false),
            Triple(R.drawable.ic_pinjam_buku, "Pinjam Buku", false),

//            Triple(R.drawable.ic_daftar_ulang, "Daftar Ulang", true),
//            Triple(R.drawable.ic_koperasi, "Koperasi", true),
//            Triple(R.drawable.ic_kantin, "Kantin", true),
        ).apply { if (!isStudent) removeAt(0) }
    }

    private val onClick by lazy {
        { position: Int ->
            if (isStudent)
                when (position) {
                    0 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(Intent(requireActivity(), SppPaymentPage::class.java))
                    else
                        showDialogActivate()

                    1 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(
                            Intent(
                                requireActivity(),
                                PartisipasiPage::class.java
                            ).putExtra("klaspayId", viewmodel.klaspayData.value?.wallet_id)
                        )
                    else
                        showDialogActivate()

                    2 -> startActivity(Intent(requireActivity(), PerpusPage::class.java))
                    else -> showDialogComingSoon()
                }
            else
                when (position) {
                    0 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(
                            Intent(
                                requireActivity(),
                                PartisipasiPage::class.java
                            ).putExtra("klaspayId", viewmodel.klaspayData.value?.wallet_id)
                        )
                    else
                        showDialogActivate()

                    1 -> startActivity(Intent(requireActivity(), PerpusPage::class.java))
                    else -> showDialogComingSoon()
                }
        }
    }

    private val ppobClickHandler by lazy {
        { position: Int ->
            if (viewmodel.isKlaspayActive.value == true)
                when (position) {
                    0 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(Intent(requireActivity(), PulsaPage::class.java))
                    else
                        showDialogActivate()

                    1 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(Intent(requireActivity(), ListrikPage::class.java))
                    else
                        showDialogActivate()

                    2 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(Intent(requireActivity(), AirPage::class.java))
                    else
                        showDialogActivate()

                    3 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(Intent(requireActivity(), InternetPage::class.java))
                    else
                        showDialogActivate()

                    4 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(Intent(requireActivity(), GamePage::class.java))
                    else
                        showDialogActivate()

                    5 -> if (viewmodel.isKlaspayActive.value == true)
                        startActivity(Intent(requireActivity(), BpjsPage::class.java))
                    else
                        showDialogActivate()

                    else -> showDialogComingSoon()
                }
            else showDialogActivate()
        }
    }
}