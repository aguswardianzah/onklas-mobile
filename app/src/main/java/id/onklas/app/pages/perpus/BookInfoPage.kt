package id.onklas.app.pages.perpus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import id.onklas.app.R
import id.onklas.app.databinding.BookInfoPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.pembayaran.RowAdapter
import id.onklas.app.utils.LinearSpaceDecoration

class BookInfoPage : Fragment() {

    private lateinit var binding: BookInfoPageBinding
    private val viewmodel by activityViewModels<PerpusViewModel> { component.perpusVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = BookInfoPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvInfo.addItemDecoration(
            LinearSpaceDecoration(
                space = resources.getDimensionPixelSize(R.dimen._4sdp)
            )
        )

        viewmodel.bookDetail.observe(viewLifecycleOwner, {
            binding.rvInfo.adapter = RowAdapter(
                arrayListOf(
                    "Tanggal terbit" to it.published_at,
                    "Jumlah halaman" to it.page,
                    "ISBN" to it.isbn,
                    "Bahasa" to it.language,
                    "Penerbit" to it.publisher
                )
            )
        })
    }
}