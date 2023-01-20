package id.onklas.app.pages.sekolah.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.onklas.app.GlideApp
import id.onklas.app.databinding.OnboardItemBinding
import id.onklas.app.databinding.StoreBannerItemBinding

class StoreBannerItem(private val imgRes: String) :
    Fragment() {
    private val glide by lazy { GlideApp.with(this) }
    private lateinit var binding: StoreBannerItemBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = StoreBannerItemBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        glide.load(imgRes)
            .centerCrop()
            .into(binding.image)
    }
}