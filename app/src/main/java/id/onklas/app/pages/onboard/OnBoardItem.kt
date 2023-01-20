package id.onklas.app.pages.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.onklas.app.databinding.OnboardItemBinding

class OnBoardItem(private val imgRes: Int, private val title: String, private val content: String) :
    Fragment() {

    private lateinit var binding: OnboardItemBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = OnboardItemBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.image.setImageResource(imgRes)
        binding.title.text = title
        binding.content.text = content
    }
}