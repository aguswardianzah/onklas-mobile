package id.onklas.app.pages.changepass

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import id.onklas.app.databinding.ChangePassPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class ChangePassPage : Privatepage() {

    companion object {
        val NOTIF_ID = 4329
    }

    private val binding by lazy { ChangePassPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ChangePassViewmodel> { component.changePassVmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationManagerCompat.from(this).cancel(NOTIF_ID)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
    }
}