package id.onklas.app.pages.onboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import id.onklas.app.R
import id.onklas.app.databinding.OnboardPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.PublicPage
import id.onklas.app.pages.login.Loginpage
import id.onklas.app.viewmodels.GeneralViewModel

class OnBoardPage : PublicPage() {

    private val viewmodel by viewModels<GeneralViewModel> { component.generalVmFactory }
    private val binding by lazy { OnboardPageBinding.inflate(layoutInflater) }
    private val items by lazy {
        listOf(
            OnBoardItem(
                R.drawable.img_onboard_1,
                "pembelajaran",
                "Kemudahan belajar kapan saja dan dimana saja, kerjakan tugas kamu hanya dalam genggaman"
            ),
            OnBoardItem(
                R.drawable.img_onboard_2,
                "media sosial",
                "Berinteraksi di sekolah kini makin seru dan update, yuk posting kegiatanmu dan pamerin kayra tulismu"
            ),
            OnBoardItem(
                R.drawable.img_onboard_3,
                "dompet digital",
                "Praktis pembayaran disekolah dengan klaspay, bayar apapun kini ga perlu ribet pakai uang cash"
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            view.systemUiVisibility = flags
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        setContentView(binding.root)

        binding.viewpager.adapter = object :
            FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment = items[position]
            override fun getCount(): Int = items.size
        }
        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.btnNext.text = if (position == items.size - 1) "selesai" else "berikutnya"
                binding.btnPrev.visibility = if (position == 0) View.GONE else View.VISIBLE
            }
        })

        binding.btnNext.setOnClickListener(onClick)
        binding.btnPrev.setOnClickListener(onClick)
    }

    private val onClick by lazy {
        View.OnClickListener {
            when (it) {
                binding.btnNext -> if (binding.viewpager.currentItem < items.size - 1) binding.viewpager.setCurrentItem(
                    binding.viewpager.currentItem + 1,
                    true
                ) else {
                    startActivity(Intent(this@OnBoardPage, Loginpage::class.java))
                    viewmodel.pref.putBoolean("onboard", true)
                    finish()
                }
                binding.btnPrev -> binding.viewpager.setCurrentItem(
                    binding.viewpager.currentItem - 1,
                    true
                )
            }
        }
    }
}