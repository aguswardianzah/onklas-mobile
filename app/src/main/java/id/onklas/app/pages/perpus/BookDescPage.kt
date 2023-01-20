package id.onklas.app.pages.perpus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import id.onklas.app.databinding.BookDescPageBinding
import id.onklas.app.di.component

class BookDescPage : Fragment() {

    private lateinit var binding: BookDescPageBinding
    private val viewmodel by activityViewModels<PerpusViewModel> { component.perpusVmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = BookDescPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewmodel.bookDetail.observe(viewLifecycleOwner, {
            binding.desc = it.description
        })
    }
}