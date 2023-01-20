package id.onklas.app.pages.sekolah

import android.annotation.SuppressLint
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.StringUtil
import kotlin.math.min

open class PostViewholder(
    view: View
) : RecyclerView.ViewHolder(view) {

    val postHeader by lazy { view.findViewById<ViewGroup>(R.id.post_header) }
    val postFooter by lazy { view.findViewById<ViewGroup>(R.id.post_footer) }
    val imgProfile by lazy { postHeader.findViewById<ImageView>(R.id.img_profile) }
    val nameProfile by lazy { postHeader.findViewById<TextView>(R.id.name) }
    val postTime by lazy { postHeader.findViewById<TextView>(R.id.post_time) }
    val option by lazy { postHeader.findViewById<MaterialButton>(R.id.option) }
    val content by lazy { view.findViewById<TextView>(R.id.content) }
    val imgLikes by lazy { postFooter.findViewById<ViewGroup>(R.id.img_like) }
    val info by lazy { postFooter.findViewById<TextView>(R.id.info) }
    val layoutLike by lazy { postFooter.findViewById<View>(R.id.rl_like) }
    val layoutComment by lazy { postFooter.findViewById<View>(R.id.rl_comment) }
    val btnLike by lazy { postFooter.findViewById<CheckBox>(R.id.ic_like) }
    val tvLike by lazy { postFooter.findViewById<TextView>(R.id.like_label) }

    @SuppressLint("SetTextI18n")
    open fun bind(
        item: FeedTimeline,
        callback: PostAdapterCallback<FeedTimeline>,
        isMine: Boolean = false,
        glide: GlideRequests,
        stringUtil: StringUtil
    ) {
        glide.load(item.userTable.user_avatar_image)
            .circleCrop()
            .thumbnail(0.1f)
            .override(100)
            .into(imgProfile)
        nameProfile.text = item.userTable.name
        nameProfile.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            if (item.userTable.is_verified) ResourcesCompat.getDrawable(
                nameProfile.context.resources,
                R.drawable.ic_check_green,
                null
            ) else null,
            null
        )
        content.text = stringUtil.buildUserContentPost(
            content.context,
            item.feed.feed_body,
            500,
            onClickMention = { username -> callback.onClickMention(username) },
            onClickLoadMore = { callback.onClickComment(item) },
            onClickHashtag = { hashtag -> callback.onClickHashtag(hashtag) }
        )
        content.movementMethod = LinkMovementMethod.getInstance()

        update(item)

        imgProfile.setOnClickListener { callback.onClickProfile(item) }
        nameProfile.setOnClickListener { callback.onClickProfile(item) }

        if (isMine) {
            option.visibility = View.VISIBLE
            option.setOnClickListener { callback.onClickOption(item) }
        } else {
            option.visibility = View.GONE
        }

        layoutLike.setOnClickListener { btnLike.performClick() }
        btnLike.setOnCheckedChangeListener { buttonView, isChecked ->
            setLike(isChecked)
            val countLike = item.feed.count_likes + (if (isChecked) 1 else -1)
            info.text = "$countLike Suka - ${item.feed.count_comments} Komentar"
            callback.onClickLike(item, isChecked)
        }

        imgLikes.setOnClickListener { callback.onClickListLike(item) }
        layoutComment.setOnClickListener { callback.onClickComment(item) }
        info.setOnClickListener { callback.onClickComment(item) }
    }

    @SuppressLint("SetTextI18n")
    open fun update(item: FeedTimeline) {
        postTime.text = item.feed.timeString
        info.text = "${item.feed.count_likes} Suka - ${item.feed.count_comments} Komentar"
        buildLikeImage(item.likes)
        setLike(item.feed.isLike)
    }

    private fun setLike(liked: Boolean) {
        if (liked) {
            layoutLike.setBackgroundResource(R.drawable.post_like_bg)
            tvLike.text = "Saya suka"
            btnLike.isChecked = true
        } else {
            layoutLike.setBackgroundResource(R.drawable.post_comment_bg)
            tvLike.text = "Sukai ini"
            btnLike.isChecked = false
        }
    }

    private fun buildLikeImage(likes: List<UserTable>) {
        imgLikes.removeAllViews()
        if (likes.isNotEmpty()) {
            imgLikes.visibility = View.VISIBLE
            val context = imgLikes.context
            val imgSize = context.resources.getDimensionPixelSize(R.dimen._24sdp)
            likes.subList(0, min(3, likes.size)).forEachIndexed { id, person ->
                imgLikes.addView(
                    ImageView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(imgSize, imgSize).apply {
                            gravity = Gravity.START
                            marginStart =
                                if (imgLikes.childCount > 0) imgLikes.childCount * (imgSize / 2) else 0
                        }
                        setBackgroundResource(R.drawable.post_like_img_bg)
                        Glide.with(imgLikes.context).load(person.user_avatar_image).circleCrop()
                            .thumbnail(0.1f).into(this)
                    }
                )
            }
        } else {
            imgLikes.visibility = View.GONE
        }
    }
}