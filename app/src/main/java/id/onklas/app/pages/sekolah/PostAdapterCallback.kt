package id.onklas.app.pages.sekolah

import android.app.Activity
import android.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.onklas.app.R
import id.onklas.app.pages.akun.ProfilePage
import id.onklas.app.pages.comment.CommentPage
import id.onklas.app.pages.jelajah.HashtagPostPage
import id.onklas.app.pages.listlike.ListLikePage
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline

sealed class PostAdapterCallback<T> {

    open fun onClickProfile(item: T) {}
    open fun onClickOption(item: T) {}
    open fun onClickLike(item: T, liked: Boolean) {}
    open fun onClickFileImage(item: T) {}
    open fun onClickComment(item: T) {}
    open fun onClickListLike(item: T) {}
    open fun onClickShare(item: T) {}
    open fun onClickDownloadEbook(item: T) {}
    open fun onClickMention(username: String) {}
    open fun onClickHashtag(hashtag: String) {}

    open class DefaultPostAdapterCallback<T> : PostAdapterCallback<T>()

    open class PostCallback(
        val activity: Activity
    ) : PostAdapterCallback<FeedTimeline>() {
        override fun onClickProfile(
            item: FeedTimeline
        ) {
            ProfilePage.open(
                activity,
                if (item.userTable.nisn_nik.isNotEmpty()) item.userTable.nisn_nik else item.userTable.nis_nik
            )
        }

        override fun onClickListLike(item: FeedTimeline) {
            ListLikePage.open(activity, item.feed.feed_id)
        }

        override fun onClickFileImage(item: FeedTimeline) {
            CommentPage.open(activity, item.feed.feed_id)
        }

        override fun onClickComment(item: FeedTimeline) {
            CommentPage.open(activity, item.feed.feed_id)
        }

        override fun onClickMention(username: String) {
            ProfilePage.open(activity, username = username)
        }

        override fun onClickHashtag(hashtag: String) {
            HashtagPostPage.open(activity, hashtag)
        }

        override fun onClickOption(item: FeedTimeline) {
            AlertDialog.Builder(activity, R.style.DialogTheme)
                .setItems(arrayOf("Hapus Post")) { dialog, which ->
                    dialog.dismiss()
                    MaterialAlertDialogBuilder(activity, R.style.DialogTheme)
                        .setMessage("Anda yakin akan menghapus post?")
                        .setPositiveButton("Hapus") { dialog1, _ ->
                            dialog1.dismiss()
                            this.onDeleteFeed(item)
                        }
                        .setNeutralButton("Batal") { dialog1, _ -> dialog1.dismiss() }
                        .show()
                }
                .show()
        }

        open fun onDeleteFeed(item: FeedTimeline) {}
    }
}