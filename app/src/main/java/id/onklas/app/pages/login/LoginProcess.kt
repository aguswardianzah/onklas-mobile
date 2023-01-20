package id.onklas.app.pages.login

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.onklas.app.R
import id.onklas.app.databinding.LoginProcessBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.changepass.ChangePassPage
import id.onklas.app.pages.home.HomePage
import id.onklas.app.pages.resetpass.ResetPassPage
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginProcess : Fragment() {

    private lateinit var binding: LoginProcessBinding
    private val viewmodel by activityViewModels<LoginViewModel> { component.loginVmFactory }

    private val colorPrimary by lazy {
        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = LoginProcessBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewmodel = viewmodel

//        viewmodel.student.observe(viewLifecycleOwner, Observer {
//            binding.kelas.text = it.`class`
//        })

        viewmodel.allowLogin.observe(viewLifecycleOwner, Observer {
            binding.btnLogin.isEnabled = it
        })

        viewmodel.password.observe(viewLifecycleOwner, Observer {
            viewmodel.allowLogin.postValue(it.isNotEmpty())
        })

        binding.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                (requireActivity() as? BasePage)?.loading(msg = "Proses login akun")
                val (success, message) = viewmodel.login()
                if (success) {
                    if (viewmodel.pref.getBoolean("default_pass"))
                        showNotifDefaultPass()

                    startActivity(Intent(requireActivity(), HomePage::class.java))
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                (requireActivity() as? BasePage)?.dismissloading()
            }
        }

        binding.icBack.setOnClickListener { findNavController().navigate(R.id.action_loginProcess_to_loginForm) }

        binding.resetPassword.movementMethod = LinkMovementMethod.getInstance()
        binding.resetPassword.text =
            SpannableStringBuilder(Html.fromHtml("Lupa password? <b>Reset</b>")).apply {
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        startActivity(Intent(requireActivity(), ResetPassPage::class.java))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = colorPrimary
                    }
                }, length - 5, length, 0)
            }

        binding.forgotLabel.movementMethod = LinkMovementMethod.getInstance()
        binding.forgotLabel.text =
            SpannableStringBuilder(Html.fromHtml("NISN/NIK terdaftar bukan milik Anda?<br /><b>Hubungi Kami</b>")).apply {
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        Timber.e("click hubungi kami 6287887219649")
                        viewmodel.intentUtil.openWhatsApp(requireActivity(), "6287887219649") {
                            viewmodel.intentUtil.openCall(requireActivity(), "6287887219649")
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = colorPrimary
                    }
                }, length - 12, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
    }

    private fun showNotifDefaultPass() {
        val you =
            if (viewmodel.pref.getBoolean("is_student")) "kamu" else "Anda"
        val content =
            "Perhatian, $you masih menggunakan password default, silahkan ubah password akun $you untuk meningkatkan keamanan data"
        NotificationManagerCompat.from(requireContext().applicationContext)
            .notify(
                ChangePassPage.NOTIF_ID,
                NotificationCompat.Builder(
                    requireContext().applicationContext,
                    getString(R.string.app_name)
                )
                    .setSmallIcon(R.drawable.ic_logo_notif)
                    .setContentTitle("Ganti Password")
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(content)
                    )
                    .setContentIntent(
                        PendingIntent.getActivity(
                            requireContext().applicationContext,
                            0,
                            Intent(requireContext().applicationContext, ChangePassPage::class.java),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()
            )
    }
}