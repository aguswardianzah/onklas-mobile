package id.onklas.app.pages.ppob.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.onklas.app.R
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.ppob.pulsa.PulsaItem
import id.onklas.app.utils.StringUtil
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class GameViewModel @Inject constructor(
    val apiService: ApiService,
    val stringUtil: StringUtil,
    val db: MemoryDB
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val listGame = mutableMapOf<String, List<PulsaItem>>()
    val listProvider by lazy { MutableLiveData<List<Pair<Int, String>>>() }
    val providerItems by lazy { MutableLiveData<List<PulsaItem>>() }
    val loadingGame by lazy { MutableLiveData(true) }
    val inputNomor by lazy { MutableLiveData<String>() }
    val pageTitle by lazy { MutableLiveData("Voucher Game") }
    val errorInput by lazy { MutableLiveData(false) }

    suspend fun loadGame() {
        try {
            loadingGame.postValue(true)
            val res = apiService.listGame().data

            listGame.clear()
            listGame.putAll(res)
            listProvider.postValue(res.keys.map { getProviderRes(it) to it })
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        } finally {
            loadingGame.postValue(false)
        }
    }

    fun selectProvider(name: String) {
        listGame[name]?.let {
            it.firstOrNull()?.let { pageTitle.postValue(it.provider_text) }
            providerItems.postValue(it)
        }
    }

    private fun getProviderRes(name: String) = when (name.toUpperCase(Locale.ROOT)) {
        "AOV" -> R.drawable.ic_aov
        "FF" -> R.drawable.ic_free_fire
        "GARENA" -> R.drawable.ic_garena
        "GEMSCOOL" -> R.drawable.ic_gemscool
        "HAGO" -> R.drawable.ic_hago
        "ITUNES" -> R.drawable.ic_itunes
        "MEGAXUS" -> R.drawable.ic_megaxus
        "ML" -> R.drawable.ic_mobile_legend
        "MOGCASH" -> R.drawable.ic_mogcash
        "PUBG" -> R.drawable.ic_pubg_mobile
        "RAZER" -> R.drawable.ic_razer
        "TIXID" -> R.drawable.ic_tixid
        "UNIPIN" -> R.drawable.ic_unipin
        "WAVEGAME" -> R.drawable.ic_wave_game
        "ZEPETTO" -> R.drawable.ic_point_blank
        else -> R.drawable.ic_voucher_game
    }
}