package id.onklas.app.pages.pembelajaran

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.ComingSoonDialogBinding
import id.onklas.app.databinding.PembelajaranAdsItemBinding
import id.onklas.app.databinding.PembelajaranBannerBinding
import id.onklas.app.databinding.PembelajaranPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BaseFragment
import id.onklas.app.pages.akm.AkmPage
import id.onklas.app.pages.announcement.AnnouncementPage
import id.onklas.app.pages.homework.HomeWorkPage
import id.onklas.app.pages.homework.teacher.HomeworkTeacherPage
import id.onklas.app.pages.magang.MagangPage
import id.onklas.app.pages.presensi.PresensiPage
import id.onklas.app.pages.prokes.ProkesPage
import id.onklas.app.pages.theory.TheoryPage
import id.onklas.app.pages.theoryteacher.TheoryTeacherPage
import id.onklas.app.pages.tryout.TryOutPage
import id.onklas.app.utils.GridSpacingItemDecoration
import id.onklas.app.utils.SnapOnScrollListener
import kotlin.math.abs

class PembelajaranPage : BaseFragment() {

    private lateinit var binding: PembelajaranPageBinding
    private val viewmodel by activityViewModels<PembelajaranVm> { component.pembelajaranVmFactory }
    private val snapHelper by lazy { PagerSnapHelper() }
    private val itemSpace by lazy { resources.getDimensionPixelSize(R.dimen._8sdp) }
    private val menuItemDecoration by lazy {
        GridSpacingItemDecoration(3, itemSpace, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        PembelajaranPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.headIndicator.count = viewmodel.listBanner.size
        binding.rvHead.apply {
            adapter = HeadAdapter(viewmodel.listBanner)
            snapHelper.attachToRecyclerView(this)
            addOnScrollListener(
                SnapOnScrollListener(
                    snapHelper, onSnapPositionChangeListener = (binding.headIndicator::setSelection)
                )
            )
        }
        binding.rvMenu.apply {
            adapter = MenuAdapter(
                mutableListOf(
                    Triple(R.drawable.ic_matery, "Materi", false),
                    Triple(R.drawable.ic_tasks, "Tugas", false),
                    Triple(R.drawable.ic_presensi, "Presensi", false),
//                    Triple(R.drawable.ic_tasks, "KlasPustaka", BuildConfig.BUILD_TYPE != "debug"),
//                    Triple(R.drawable.ic_osis, "Osis", true),
                    Triple(R.drawable.ic_announcement, "Berita", false),
                    Triple(R.drawable.ic_test, "Ujian", !viewmodel.pref.getBoolean("is_student")),
                    Triple(R.drawable.ic_announcement, "Magang", false),
//                    Triple(R.drawable.ic_akm, "Diknas", !viewmodel.pref.getBoolean("is_student")),
//                    Triple(R.drawable.ic_prokes, "Prokes", false),
//                    Triple(
//                        R.drawable.ic_try_out, "Quiz", !viewmodel.pref.getBoolean("is_student")
//                    )
                ).apply {
                    if (!viewmodel.pref.getBoolean("is_student")) removeAt(4)
                }, menuClickHandler
            )
            addItemDecoration(menuItemDecoration)
        }

        binding.rvAds.apply {
            adapter = AdsAdapter()
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.right = itemSpace / 2
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.left = itemSpace / 2
                    }
                }
            })
            onFlingListener = object : RecyclerView.OnFlingListener() {
                override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                    if (abs(velocityX) > 0.8) {
                        fling(velocityX / 2, velocityY)
                        return true
                    }

                    return false
                }
            }
        }
    }

    private inner class HeadAdapter(val list: List<Any>) :
        RecyclerView.Adapter<HeadViewholder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadViewholder =
            HeadViewholder(parent)

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: HeadViewholder, position: Int) =
            holder.bind(list[position])
    }

    private inner class HeadViewholder(
        parent: ViewGroup,
//        val binding: PembelajaranHeadItemBinding = PembelajaranHeadItemBinding.inflate(
        val binding: PembelajaranBannerBinding = PembelajaranBannerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.clipToOutline = true
        }

        fun bind(img: Any) {
            binding.item = img
            binding.executePendingBindings()
        }
    }

    private val menuClickHandler by lazy {
        { pos: Int, view: View ->
            when (pos) {
                0 -> startActivity(
                    Intent(
                        requireActivity(),
                        if (viewmodel.pref.getBoolean("is_student"))
                            TheoryPage::class.java
                        else
                            TheoryTeacherPage::class.java
                    )
                )
                1 -> startActivity(
                    Intent(
                        requireActivity(),
                        if (viewmodel.pref.getBoolean("is_student")) HomeWorkPage::class.java
                        else HomeworkTeacherPage::class.java
                    )
                )

                2 -> if (viewmodel.pref.getBoolean("klastime")) startActivity(
                    Intent(
                        requireActivity(),
                        PresensiPage::class.java
                    )
                ) else prettyAlert(
                    titleText = "Fitur tidak dapat digunakan",
                    msg = "Silahkan hubungi sekolah untuk mengaktifkan fitur presensi",
                    abortLabel = ""
                )

//                3 -> if (BuildConfig.BUILD_TYPE == "debug")
//                    startActivity(Intent(requireActivity(), PerpusPage::class.java))
//                else showDialogComingSoon()

                3 -> startActivity(
                    Intent(requireActivity(), AnnouncementPage::class.java)
                )
                4 -> if (viewmodel.pref.getBoolean("is_student")) startActivity(
                    Intent(
                        requireActivity(),
//                        UjianPage::class.java
                        AkmPage::class.java
                    ).putExtra("isSchoolScope", true)
                ) else showDialogComingSoon()
                5 -> startActivity(
                    Intent(requireActivity(), MagangPage::class.java)
                )
//                6 -> if (viewmodel.pref.getBoolean("is_student")) startActivity(
//                    Intent(
//                        requireActivity(),
//                        AkmPage::class.java
//                    )
//                ) else showDialogComingSoon()
////                7 -> if (viewmodel.pref.getBoolean("is_student")) startActivity(
////                        Intent(
////                                requireActivity(),
////                                EntrepreneursPage::class.java
////                        )
////                ) else showDialogComingSoon()
//                7 -> startActivity(Intent(requireActivity(), ProkesPage::class.java))
//                8 -> if (viewmodel.pref.getBoolean("is_student")) startActivity(
//                    Intent(
//                        requireActivity(),
//                        TryOutPage::class.java
//                    )
//                ) else showDialogComingSoon()
                else -> showDialogComingSoon()
            }
        }
    }

    private fun showDialogComingSoon() {
        val dialogBinding = ComingSoonDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_white)
        dialogBinding.btnOk.setOnClickListener { dialog.dismiss() }
    }

    private inner class AdsAdapter(
        val list: List<Any> = viewmodel.listEdu
    ) : RecyclerView.Adapter<AdsViewholder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsViewholder =
            AdsViewholder(parent)

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: AdsViewholder, position: Int) =
            holder.bind(list[position])
    }

    private inner class AdsViewholder(
        parent: ViewGroup,
        val binding: PembelajaranAdsItemBinding = PembelajaranAdsItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.image.clipToOutline = true
        }

        fun bind(url: Any) {
            binding.url = url
        }
    }
}