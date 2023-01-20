package id.onklas.app.pages.akun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.ProfileMediaPageBinding
import id.onklas.app.di.component
import id.onklas.app.utils.GridSpacingItemDecoration
import kotlinx.coroutines.launch

class ProfileMediaPage : Fragment() {

    private lateinit var binding: ProfileMediaPageBinding
    private val viewmodel by activityViewModels<ProfileViewModel> { component.profileVmFactory }
    private val adapter by lazy { PostGridAdapter(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        ProfileMediaPageBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvPost.adapter = adapter
        binding.rvPost.addItemDecoration(
            GridSpacingItemDecoration(
                3,
                1,
                false
            )
        )

        viewmodel.userData.observe(viewLifecycleOwner, {
            viewmodel.listPost(it.data.id, "image")
                .observe(viewLifecycleOwner, Observer(adapter::submitList))

            lifecycleScope.launch {
                viewmodel.prevImageId = -1
                viewmodel.loadListImage(0)
            }
        })
    }
}