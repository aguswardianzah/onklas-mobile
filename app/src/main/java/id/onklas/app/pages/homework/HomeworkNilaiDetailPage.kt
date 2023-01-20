package id.onklas.app.pages.homework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.onklas.app.databinding.HomeworkNilaiDetailPageBinding
import id.onklas.app.pages.PageDialogFragment

class HomeworkNilaiDetailPage(val item: HomeworkItemTable) : PageDialogFragment() {

    private lateinit var binding: HomeworkNilaiDetailPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = HomeworkNilaiDetailPageBinding.inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.item = item
        binding.executePendingBindings()
    }
}