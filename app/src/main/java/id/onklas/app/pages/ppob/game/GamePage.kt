package id.onklas.app.pages.ppob.game

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import id.onklas.app.databinding.GamePageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.Privatepage

class GamePage : Privatepage() {

    private val binding by lazy { GamePageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<GameViewModel> { component.gameVmFactory }

    init {
        lifecycleScope.launchWhenCreated {
            viewmodel.loadGame()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            binding.toolbar.setNavigationOnClickListener { finish() }
        }

        viewmodel.errorString.observe(this, Observer(this::toast))
    }
}