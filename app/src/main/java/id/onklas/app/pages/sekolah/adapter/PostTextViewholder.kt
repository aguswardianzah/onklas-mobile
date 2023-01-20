package id.onklas.app.pages.sekolah.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.PostAdapterCallback
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.pages.sekolah.sosmed.UserTable
import id.onklas.app.utils.StringUtil
import kotlin.math.min

open class PostTextViewholder(view: View) : RecyclerView.ViewHolder(view) {

    val imgProfile by lazy { view.findViewById<ImageView>(R.id.img_profile) }
    val nameProfile by lazy { view.findViewById<TextView>(R.id.name) }
    val postTime by lazy { view.findViewById<TextView>(R.id.post_time) }
    val option by lazy { view.findViewById<MaterialButton>(R.id.option) }
    val content by lazy { view.findViewById<TextView>(R.id.content) }
    val imgLikes by lazy { view.findViewById<ViewGroup>(R.id.img_like) }
    val info by lazy { view.findViewById<TextView>(R.id.info) }
    val btnLike by lazy { view.findViewById<MaterialButton>(R.id.btn_like) }
    val btnComment by lazy { view.findViewById<MaterialButton>(R.id.btn_comment) }

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

        if (item.feed.feed_body.isNotEmpty()) {
            content.text = stringUtil.buildUserContentPost(
                content.context,
                item.feed.feed_body,
                100,
                onClickMention = { username -> callback.onClickMention(username) },
                onClickLoadMore = { callback.onClickComment(item) },
                onClickHashtag = { hashtag -> callback.onClickHashtag(hashtag) }
            )
            content.movementMethod = LinkMovementMethod.getInstance()
            content.visibility = View.VISIBLE
        } else {
            content.visibility = View.GONE
        }

        update(item, glide, callback)

        imgProfile.setOnClickListener { callback.onClickProfile(item) }
        nameProfile.setOnClickListener { callback.onClickProfile(item) }

//        Timber.e("${item.userTable.name} writes: ${item.feed.feed_body}\nisMine: $isMine")
        if (isMine) {
            option.visibility = View.VISIBLE
            option.setOnClickListener { callback.onClickOption(item) }
        } else {
            option.visibility = View.GONE
        }

        imgLikes.setOnClickListener { callback.onClickListLike(item) }
        btnComment.setOnClickListener { callback.onClickComment(item) }
        info.setOnClickListener { callback.onClickComment(item) }
    }

    @SuppressLint("SetTextI18n")
    open fun update(
        item: FeedTimeline,
        glide: GlideRequests,
        callback: PostAdapterCallback<FeedTimeline>
    ) {
        postTime.text = item.feed.timeString
        info.text =
            (if (item.feed.count_likes > 0) "${item.feed.count_likes} Suka - " else "") + "${item.feed.count_comments} Komentar"
        buildLikeImage(item.likes, glide)
        setLike(item.feed.isLike)

        btnLike.setOnClickListener {
            val isLike = !item.feed.isLike
            setLike(!item.feed.isLike)
            val countLike = item.feed.count_likes + (if (isLike) 1 else -1)
            info.text =
                (if (countLike > 0) "$countLike Suka - " else "") + "${item.feed.count_comments} Komentar"
            callback.onClickLike(item, isLike)
        }
    }

    @SuppressLint("SetTextI18n")
    fun setLike(liked: Boolean) {
        if (liked) {
            btnLike.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFDFE4"))
            btnLike.text = "Saya suka"
            btnLike.icon =
                ResourcesCompat.getDrawable(btnLike.context.resources, R.drawable.ic_liked, null)
        } else {
            btnLike.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F4F5F6"))
            btnLike.text = "Sukai ini"
            btnLike.icon =
                ResourcesCompat.getDrawable(btnLike.context.resources, R.drawable.ic_like, null)
        }
    }

    private fun buildLikeImage(likes: List<UserTable>, glide: GlideRequests) {
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
                        glide.load(person.user_avatar_image)
                            .circleCrop()
                            .override(50)
                            .thumbnail(0.1f)
                            .into(this)
                    }
                )
            }
        } else {
            imgLikes.visibility = View.GONE
        }
    }
}