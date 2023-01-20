package id.onklas.app.pages.sekolah

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rd.PageIndicatorView
import id.onklas.app.GlideRequests
import id.onklas.app.R
import id.onklas.app.pages.sekolah.sosmed.FeedTimeline
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import kotlin.math.min

class PostMediaViewholder(
    view: View,
    private val viewPool: RecyclerView.RecycledViewPool
) :
    PostViewholder(view) {

    private val postMedia: ViewGroup by lazy { view.findViewById<ViewGroup>(R.id.post_media) }
    private val rvMedia: RecyclerView by lazy { postMedia.findViewById<RecyclerView>(R.id.rv_media) }
    private val counter: TextView by lazy { postMedia.findViewById<TextView>(R.id.counter) }
    private val indicator: PageIndicatorView by lazy { postMedia.findViewById<PageIndicatorView>(R.id.media_indicator) }

//    private val snapHelper by lazy { LinearSnapHelper() }
//    private val handler by lazy { Handler() }
//    private val hideCounter by lazy { Runnable { counter.visibility = View.GONE } }

    private var mediaAdapter: MediaAdapter? = null

    override fun bind(
        item: FeedTimeline,
        callback: PostAdapterCallback<FeedTimeline>,
        isMine: Boolean,
        glide: GlideRequests,
        stringUtil: StringUtil
    ) {
        super.bind(item, callback, isMine, glide, stringUtil)

        Timber.e(
            "bind post ${
                item.feed.feed_body.subSequence(
                    0,
                    min(20, item.feed.feed_body.length)
                )
            }, postId: ${item.feed.feed_id}, with file: ${item.files}"
        )

        rvMedia.apply {
            (layoutManager as? LinearLayoutManager)?.apply { initialPrefetchItemCount = 1 }
            setRecycledViewPool(viewPool)
            mediaAdapter?.let {
                it.item = item
                adapter = it
                it.notifyDataSetChanged()
            } ?: run {
                mediaAdapter = MediaAdapter(item, callback::onClickFileImage, glide)
                adapter = mediaAdapter
            }
            rvMedia.layoutParams = rvMedia.layoutParams.apply {
                height = width
            }
            requestLayout()

//            if (item.media.size > 1) {
//                indicator.visibility = View.VISIBLE
//                counter.visibility = View.VISIBLE
//                indicator.count = item.media.size
//                try {
//                    snapHelper.attachToRecyclerView(this)
//                    addOnScrollListener(
//                        SnapOnScrollListener(
//                            snapHelper,
//                            SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_STATE_IDLE
//                        ) {
//                            indicator.selection = it
//                            counter.text = "${it + 1}/${item.media.size}"
//                            counter.visibility = View.VISIBLE
//                            handler.postDelayed(hideCounter, 3000)
//                        })
//                } catch (e: Exception) {
//                }
//            } else {
//            indicator.visibility = View.GONE
//            counter.visibility = View.GONE
//            }
        }

        indicator.visibility = View.GONE
        counter.visibility = View.GONE
    }
}