package id.onklas.app.pages.jelajah

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.Navigation
import id.onklas.app.R
import id.onklas.app.databinding.JelajahPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.sekolah.MyImageZoom
import timber.log.Timber

class JelajahPage : BasePage() {

    private val binding by lazy { JelajahPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<JelajahViewModel> { component.jelajahVmFactory }
    private val navController by lazy {
        Navigation.findNavController(this, R.id.page_container)
    }
    private val colorPrimary by lazy { ContextCompat.getColor(this, R.color.colorPrimary) }
    private val colorGray by lazy { ContextCompat.getColor(this, R.color.gray) }
    private val buttons by lazy { arrayOf(binding.btnPopuler, binding.btnUser, binding.btnHashtag) }

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val postSearch by lazy {
        Runnable {
            viewmodel.search.postValue(binding.inCari.text.toString())
            viewmodel.loadingPopular.postValue(true)
            viewmodel.loadingUser.postValue(true)
            viewmodel.loadingHashtag.postValue(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.btnPopuler.setOnClickListener {
            navController.navigate(R.id.action_global_jelajahPopularPage)
            setButton(it)
        }
        binding.btnUser.setOnClickListener {
            navController.navigate(R.id.action_global_jelajahUserPage)
            setButton(it)
        }
        binding.btnHashtag.setOnClickListener {
            navController.navigate(R.id.action_global_jelajahHashtagPage)
            setButton(it)
        }

        binding.inCari.doAfterTextChanged {
            handler.removeCallbacks(postSearch)
            handler.postDelayed(postSearch, 500)
        }

        binding.inCari.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(postSearch)
                handler.postDelayed(postSearch, 10)
            }
            true
        }

        binding.swipeRefresh.setOnRefreshListener {
            handler.removeCallbacks(postSearch)
            handler.postDelayed(postSearch, 10)
        }

        viewmodel.search.observe(this, {
            binding.btnPopuler.text = if (it.isEmpty()) "Terpopuler" else "Post"
        })

        binding.lifecycleOwner = this
        viewmodel.loadingPopular.observe(this, {
            Timber.e("loading populer: $it")
            if (!it)
                binding.swipeRefresh.isRefreshing = false
        })

        viewmodel.loadingUser.observe(this, {
            Timber.e("loading user: $it")
            if (!it)
                binding.swipeRefresh.isRefreshing = false
        })

        viewmodel.loadingHashtag.observe(this, {
            Timber.e("loading hashtag: $it")
            if (!it)
                binding.swipeRefresh.isRefreshing = false
        })
    }

    private fun setButton(btn: View) {
        buttons.forEach {
            if (btn == it) {
                it.setStrokeColorResource(R.color.colorPrimary)
                it.setTextColor(colorPrimary)
            } else {
                it.setStrokeColorResource(R.color.gray)
                it.setTextColor(colorGray)
            }
        }
    }

    private val zoomHelper: MyImageZoom by lazy { MyImageZoom(this, binding.root) }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return zoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        finish()
    }
}