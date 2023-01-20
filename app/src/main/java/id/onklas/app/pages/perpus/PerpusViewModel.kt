package id.onklas.app.pages.perpus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.squareup.moshi.Moshi
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.db.OnKlasDbUtil
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PerpusViewModel @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi,
    val db: MemoryDB,
    val dbUtil: OnKlasDbUtil,
    val apiService: ApiService
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }
    val pageSize = 20

    val listBanner by lazy { MutableLiveData<List<PerpusBanner>>(emptyList()) }

    suspend fun initBanner() = listBanner.postValue(fetchBanner())

    private suspend fun fetchBanner(): List<PerpusBanner> = try {
        apiService.perpusBanner()
            .data
            .also { db.perpus().insertBanner(it) }
    } catch (e: Exception) {
        db.perpus().listBannerAll()
    }

    val sections = flow {
        emit(0 to getNewest())
        emit(1 to getBest())
    }
        .filter { it.second.isNotEmpty() }
        .map {
            BookSection(
                it.first,
                "Buku ${if (it.first == 0) "Terbaru" else "Terlaris"}",
                it.second
            )
        }

    private suspend fun getNewest() = try {
        apiService.bookNewest()
            .data
            .map { BookTable.fromBookItem(it, "newest") }
            .also { db.perpus().insertBook(it) }
    } catch (e: Exception) {
        errorString.postValue(e.localizedMessage)
        Timber.e(e)
        db.perpus().getNewest()
    }

    private suspend fun getBest() = try {
        apiService.bookBest()
            .data
            .map { BookTable.fromBookItem(it, "best") }
            .also { db.perpus().insertBook(it) }
    } catch (e: Exception) {
        errorString.postValue(e.localizedMessage)
        Timber.e(e)
        db.perpus().getBest()
    }

    val loadingSearchBook by lazy { MutableLiveData(true) }
    val listSearchBook by lazy { MutableLiveData<List<BookTable>>(emptyList()) }
    suspend fun searchBook(search: String = "") = listSearchBook.postValue(
        try {
            loadingSearchBook.postValue(true)
            apiService.searchBook(search)
                .data
                .map { BookTable.fromBookItem(it) }
                .also { db.perpus().insertBook(it) }
        } catch (e: Exception) {
            errorString.postValue(e.localizedMessage)
            Timber.e(e)
            db.perpus().searchBook("%${search}%")
        } finally {
            loadingSearchBook.postValue(false)
        }
    )

    val bookDetail by lazy { MutableLiveData<BookTable>() }
    suspend fun getDetail(id: Int) = try {
        db.perpus().getBook(id).also { bookDetail.postValue(it.firstOrNull()) }.isNotEmpty()
    } catch (e: Exception) {
        errorString.postValue(e.localizedMessage)
        Timber.e(e)
        false
    }

    suspend fun getStock(id: Int) = try {
        apiService.bookStock(id)
            .data.tab.let { it.available to it.stock }
    } catch (e: Exception) {
        errorString.postValue(e.localizedMessage)
        Timber.e(e)
        0 to 0
    }

    val totalRent by lazy { MutableLiveData(0) }
    val totalRentLate by lazy { MutableLiveData(0) }
    val listRent by lazy {
        db.perpus().getRentOngoing()
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback<BookRentTable>(
                    {
                        viewModelScope.launch {
                            getListRent()
                        }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.perpus().countRentOngoing()
                            if (hasNextRent && count >= pageSize)
                                getListRent(count)
                        }
                    }
                )
            )
    }
    val listReturn by lazy {
        db.perpus().getRentFinish()
            .toLiveData(
                pageSize,
                boundaryCallback = PagedListBoundaryCallback<BookRentTable>(
                    {
                        viewModelScope.launch {
                            getHistory()
                        }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.perpus().countRentFinish()
                            if (hasNextHistory && count >= pageSize)
                                getHistory(count)
                        }
                    }
                )
            )
    }

    var hasNextHistory = true
    var lastStartHistory = -1
    val loadingHistory by lazy { MutableLiveData(true) }
    suspend fun getHistory(start: Int = 0) {
        if (start > lastStartHistory) lastStartHistory = start
        else return

        try {
            if (start == 0) loadingHistory.postValue(true)

            val list = apiService.rentHistory(pageSize, start).data

            hasNextHistory = dbUtil.processBookRent(list) && list.size >= pageSize
        } catch (e: Exception) {
            errorString.postValue(e.localizedMessage)
            Timber.e(e)
        } finally {
            if (start == 0) loadingHistory.postValue(false)
        }
    }

    var hasNextRent = true
    var lastStartRent = -1
    val loadingRent by lazy { MutableLiveData(true) }
    suspend fun getListRent(start: Int = 0) {
        if (start > lastStartRent) lastStartRent = start
        else return

        try {
            if (start == 0) loadingRent.postValue(true)

            val resp = apiService.rentOngoing(pageSize, start)

            totalRent.postValue(resp.total_rent)
            totalRentLate.postValue(resp.total_rent_late)
            hasNextRent = dbUtil.processBookRent(resp.data) && resp.data.size >= pageSize
        } catch (e: Exception) {
            errorString.postValue(e.localizedMessage)
            Timber.e(e)
        } finally {
            if (start == 0) loadingRent.postValue(false)
        }
    }
}