package id.onklas.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import id.onklas.app.GlideApp
import id.onklas.app.api.ApiService
import id.onklas.app.databinding.LinkPreviewBinding
import org.jsoup.Jsoup
import timber.log.Timber

class LinkPreview : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val binding = LinkPreviewBinding.inflate(LayoutInflater.from(context), this, false)

    suspend fun loadUrl(
        api: ApiService,
        url: String,
        onFinish: (success: Boolean, meta: LinkMeta?) -> Unit = { _, _ -> }
    ) = try {
        if (childCount == 0)
            addView(binding.root)

        binding.loadingLink.isVisible = true
        binding.loadingLinkLabel.isVisible = true

        binding.url.isVisible = false
        binding.title.isVisible = false
        binding.desc.isVisible = false
        binding.image.isVisible = false
        binding.line.isVisible = false

        val src = api.download(url).byteStream()
        val doc = Jsoup.parse(src, "utf-8", url)

        var title = doc.select("meta[property=og:title]").attr("content")
        if (title.isNullOrEmpty()) {
            title = doc.title()
        }

        var desc = doc.select("meta[name=description]").attr("content")
        if (desc.isNullOrEmpty())
            desc = doc.select("meta[name=Description]").attr("content")
        if (desc.isNullOrEmpty())
            desc = doc.select("meta[property=og:description]").attr("content")

        val images = doc.select("meta[property=og:image]")
        var image = ""
        image = if (images.size > 0)
            images.attr("content")
        else
            doc.select("link[rel=image_src]").attr("href")

        if (image.isEmpty())
            image = doc.select("link[rel=apple-touch-icon]").attr("href")
        if (image.isEmpty())
            image = doc.select("link[rel=icon]").attr("href")

        val meta = LinkMeta(url, title, image, desc)
        currMeta = meta

        binding.url.text = meta.url
        binding.title.text = meta.title
        binding.desc.text = meta.desc
        GlideApp.with(binding.image).load(meta.image).into(binding.image)

        binding.title.isVisible = true
        binding.desc.isVisible = true
        binding.image.isVisible = true
        binding.line.isVisible = true

        binding.brokenLink.isVisible = false

        onFinish.invoke(true, meta)
    } catch (e: Exception) {
        Timber.e(e)

        binding.line.isVisible = false
        binding.title.isVisible = false
        binding.desc.isVisible = false
        binding.image.isVisible = false

        binding.brokenLink.isVisible = true

        onFinish.invoke(false, null)
    } finally {
        binding.url.isVisible = true
        binding.loadingLink.isVisible = false
        binding.loadingLinkLabel.isVisible = false

        binding.root.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    var currMeta: LinkMeta? = null

    fun onClick(handle: () -> Unit) = binding.root.setOnClickListener {
        currMeta?.let {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
        }
        handle.invoke()
    }

    data class LinkMeta(
        val url: String = "",
        val title: String = "",
        val image: String = "",
        val desc: String = ""
    )
}