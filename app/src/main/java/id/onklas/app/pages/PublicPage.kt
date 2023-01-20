package id.onklas.app.pages

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import id.onklas.app.R

open class PublicPage : BasePage() {

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
    }
}