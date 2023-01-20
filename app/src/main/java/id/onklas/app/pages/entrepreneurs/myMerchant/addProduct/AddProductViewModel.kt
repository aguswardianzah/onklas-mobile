package id.onklas.app.pages.entrepreneurs.myMerchant.addProduct

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.pages.sekolah.store.*
import id.onklas.app.utils.FileUtils
import id.onklas.app.utils.IntentUtil
import id.onklas.app.utils.PagedListBoundaryCallback
import id.onklas.app.utils.PreferenceClass
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class AddProductViewModel @Inject constructor(
    val api: ApiService,
    val db: MemoryDB,
    val intentUtil: IntentUtil,
    val pref: PreferenceClass,
    val fileUtils: FileUtils
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }
    val isLoading by lazy { MutableLiveData(false) }
    val errorLoadingDetail by lazy { MutableLiveData<Boolean>() }
    val loadingMsg by lazy { MutableLiveData("menampilkan detail barang") }

    val productId by lazy { MutableLiveData(0) }
    val productName by lazy { MutableLiveData<String>() }
    val productPublished by lazy { MutableLiveData<Boolean>(true) }
    val productDesc by lazy { MutableLiveData<String>() }
    val productPrice by lazy { MutableLiveData<String>() }
    val productStock by lazy { MutableLiveData<String>() }
    val productCategory by lazy { MutableLiveData<Int>() }
    val productCategoryParent by lazy { MutableLiveData<Int>() }
    val productCategoryName by lazy { MutableLiveData<String>() }
    val productImages by lazy { MutableLiveData((0..5).map { "" }.toList()) }

    val imagesSource by lazy { mutableListOf<MyProductImage>() }
    val imagesCurrent by lazy { mutableListOf<MyProductImage>() }
    private val imagesDeletedIds by lazy { mutableListOf<Int>() }

    fun allowSave() = !productName.value.isNullOrBlank() &&
            !productDesc.value.isNullOrBlank() &&
            !productPrice.value.isNullOrBlank() &&
            !productStock.value.isNullOrBlank() &&
            productCategory.value ?: 0 > 0

    private val pageSize = 20

    private lateinit var productData: MyProductData

    fun loadDetailProduct(goodieId: Int) {
        if (isLoading.value == true)
            return

        viewModelScope.launch {
            try {
                loadingMsg.postValue("menampilkan detail barang")
                isLoading.postValue(true)

                val data = api.viewGood(goodieId).data
                productData = data
                productId.postValue(goodieId)

                productName.postValue(data.name)
                productDesc.postValue(data.description)
                productPublished.postValue(data.published == 1)
                productPrice.postValue(data.price.toString())
                productStock.postValue(data.stock.toString())
                productCategoryParent.postValue(data.category.id)
                productCategory.postValue(data.sub_category.id)
                productCategoryName.postValue(data.sub_category.name)

                imagesSource.clear()
                imagesSource.addAll(data.image.sortedBy { it.sequence })

                imagesCurrent.clear()
                imagesCurrent.addAll(imagesSource)

                setProductImages(imagesSource)
                errorLoadingDetail.postValue(false)
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
                errorLoadingDetail.postValue(true)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun deleteImage(pos: Int) {
        if (pos < imagesCurrent.size) {
            imagesDeletedIds.add(imagesCurrent[pos].id)
            imagesCurrent[pos].image = ""
        }

        setProductImages(imagesCurrent)
    }

    fun updateImage(pos: Int, path: String) {
        if (pos < imagesCurrent.size) {
            imagesCurrent[pos].image = path
        } else {
            imagesCurrent.add(MyProductImage(image = path))
        }

        setProductImages(imagesCurrent)
    }

    private fun setProductImages(imagesSrc: List<MyProductImage>) {
        val images = productImages.value!!.toMutableList()
        imagesSrc.forEachIndexed { index, myProductImage ->
            images[index] = myProductImage.image
        }
        productImages.postValue(images)
    }

    val mainCategory by lazy {
        db.store().getCategory().toLiveData(pageSize, boundaryCallback = PagedListBoundaryCallback(
            { viewModelScope.launch { fetchMainCategory() } }
        ))
    }

    var currentChildCategory: LiveData<PagedList<CategorySubTable>>? = null

    fun setCurrentCategory(parentId: Int) {
        currentChildCategory = childCategory(parentId)
    }

    fun childCategory(parentId: Int) = db.store().getCategorySub(parentId).toLiveData(
        pageSize,
        boundaryCallback = PagedListBoundaryCallback(
            { viewModelScope.launch { fetchChildCategory(parentId) } },
            {
                viewModelScope.launch {
                    val count = db.store().countCategorySub(parentId)
                    if (count >= pageSize)
                        fetchChildCategory(parentId, count)
                }
            }
        )
    )

    suspend fun fetchMainCategory() = try {
        isLoading.postValue(true)

        db.store().insertCategory(api.loadCategory().data.map { CategoryTable(it) })
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    } finally {
        isLoading.postValue(false)
    }

    var isLoadingChild = false
    var lastPosChild = -1
    var hasMoreChild = true
    suspend fun fetchChildCategory(parentId: Int, start: Int = 0) {
        if (isLoadingChild || lastPosChild == start || !hasMoreChild)
            return

        isLoadingChild = true
        lastPosChild = start

        try {
            if (start == 0)
                isLoading.postValue(true)

            val data = api.loadCategorySub(parentId, pageSize, start).data
            db.store().insertCategorySub(data.map { CategorySubTable(parentId, it) })

            hasMoreChild = data.size >= pageSize
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
            hasMoreChild = false
        } finally {
            isLoadingChild = false
            if (start == 0)
                isLoading.postValue(false)
        }
    }

    private fun hasChange() =
        (this::productData.isInitialized ||
                productData.name != productName.value ||
                productData.description != productDesc.value ||
                productData.price != productPrice.value?.toInt() ||
                productData.stock != productStock.value?.toInt() ||
                (productData.published == 1) != productPublished.value ||
                productData.category.id != productCategoryParent.value ||
                productData.sub_category.id != productCategory.value).also { Timber.e("has change: $it") }

    val isSuccessSave by lazy { MutableLiveData<Boolean>() }
    fun saveProduct() {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val goodieId = productId.value ?: 0
                Timber.e("save goodie id $goodieId")
                if (goodieId == 0) {
                    val typePlain = "text/plain".toMediaTypeOrNull()
                    val data = mapOf(
                        "name" to productName.value.orEmpty().toRequestBody(typePlain),
                        "description" to productDesc.value.orEmpty().toRequestBody(typePlain),
                        "price" to productPrice.value.orEmpty().toRequestBody(typePlain),
                        "stock" to productStock.value.orEmpty().toRequestBody(typePlain),
                        "published" to productPublished.value.toString().toRequestBody(typePlain),
                        "enterpreneur_category_id" to productCategoryParent.value.toString()
                            .toRequestBody(typePlain),
                        "enterpreneur_sub_category_id" to productCategory.value.toString()
                            .toRequestBody(typePlain)
                    )
                    val images =
                        productImages.value?.filter { it.isNotBlank() }?.mapIndexed { index, s ->
                            Timber.e("path: $s")
                            val file = File(Uri.parse(s).path.orEmpty())
                            val fileType =
                                MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                                    ?.toMediaTypeOrNull()

                            MultipartBody.Part.createFormData(
                                "file[$index]", file.name, file.asRequestBody(fileType)
                            )
                        }

                    val resp = api.createGood(data, images).data
                    db.store().insertProduct(
                        ProductTable(
                            "${resp.id}",
                            resp.id,
                            resp.name,
                            resp.description,
                            price = resp.price.toInt(),
                            stock = resp.stock.toInt(),
                            image = productImages.value?.firstOrNull { it.isNotBlank() }.orEmpty(),
                            category_id = productCategoryParent.value ?: 0,
                            sub_category_id = productCategory.value ?: 0,
                            merchant_id = pref.getInt("merchantId"),
                                productPosition = "mystore"
                        )
                    )
                } else {
                    // check if data has change
                    if (hasChange()) {
                        val resp = api.updateGood(
                            goodieId, mapOf(
                                "name" to productName.value,
                                "description" to productDesc.value,
                                "price" to productPrice.value?.toInt(),
                                "stock" to productStock.value?.toInt(),
                                "published" to productPublished.value,
                                "enterpreneur_category_id" to productCategoryParent.value,
                                "enterpreneur_sub_category_id" to productCategory.value
                            )
                        )
//                        db.store().deleteProduct("", "mystore",goodieId )
                        db.store().updateProduct(
                            ProductTable(
                                "${goodieId}",
                                resp.id,
                                resp.name,
                                resp.description,
                                price = resp.price.toInt(),
                                stock = resp.stock.toInt(),
                                image = productImages.value?.firstOrNull { it.isNotBlank() }
                                    .orEmpty(),
                                category_id = productCategoryParent.value ?: 0,
                                sub_category_id = productCategory.value ?: 0,
                                merchant_id = pref.getInt("merchantId"),
                                    productPosition = "mystore"

                            )
                        )
                    }

                    // check if images has changes
                    if (imagesCurrent != imagesSource) {
                        val srcIds = imagesSource.map { it.id }.also { Timber.e("src ids: $it") }

                        // delete images
                        imagesDeletedIds.forEach { api.deleteImageGood(goodieId, it) }

                        Timber.e("minus: ${imagesCurrent.minus(imagesSource)}")

                        // loop on current images
                        imagesCurrent.forEach {
                            // create file multipart
                            val file = File(Uri.parse(it.image).path.orEmpty())
                            val fileType =
                                MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
                                    ?.toMediaTypeOrNull()

                            val fileData = MultipartBody.Part.createFormData(
                                "file", file.name, file.asRequestBody(fileType)
                            )

                            // if id is 0 --> add new image
                            if (it.id == 0)
                                api.addImageGood(goodieId, fileData)
                            // else if src contains current img id && image not same --> update image
                            else if (srcIds.contains(it.id) && imagesSource.first { src -> src.id == it.id }.image != it.image)
                                api.updateImageGood(goodieId, it.id, fileData)
                        }
                    }
                }

                isSuccessSave.postValue(true)
            } catch (e: Exception) {
                Timber.e(e)
                errorString.postValue(e.message)
                isSuccessSave.postValue(false)
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}