package id.onklas.app.pages.sekolah.store

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import id.onklas.app.api.ApiService
import id.onklas.app.db.MemoryDB
import id.onklas.app.utils.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class StoreVm @Inject constructor(
    val apiService: ApiService,
    val apiWrapper: ApiWrapper,
    val numberUtil: NumberUtil,
    val stringUtil: StringUtil,
    val db: MemoryDB,
    val pref: PreferenceClass
) : ViewModel() {

    val errorString by lazy { MutableLiveData<String>() }

    val searchType by lazy { MutableLiveData("Produk") }

    val img_dev = "https://placeimg.com/100/100/any"

    // 1, category
    // 2, produk propuler
    // 3, kumpulan jasa
    // 4, produk terbaru
    // 5, produk terlaris
    // 6, produk sma

    val categoryList by lazy {
        listOf(
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
            CategoryItem(
                0,
                "test1",
                "https://placeimg.com/100/100/any",
            ),
        )
    }

    val product by lazy {
        listOf(
            Product(
                "00",
                "Cetaphill Gentle Skin Cleanser 59 ML",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Sarung Tangan Jempol Untuk Main PUBG  ",
                "https://picsum.photos/200/200",
                "1",
                "100",
                "200",
            ),
            Product(
                "00",
                "Cetaphill Gentle Skin Cleanser 59 ML",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Sarung Tangan Jempol Untuk Main PUBG  ",
                "https://picsum.photos/200/200",
                "1",
                "100",
                "200",
            ),
        )
    }

    val productJasa by lazy {
        listOf(
            Product(
                "00",
                "Cetaphill Gentle Skin Cleanser 59 ML",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Cetaphill Gentle Skin Cleanser 59 ML",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Sarung Tangan Jempol Untuk Main PUBG  ",
                "https://picsum.photos/200/200",
                "1",
                "100",
                "200",
            ),
            Product(
                "",
                "Lem Kain Fashion Glue Kerajinan Tangan Fab...",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Ring Rotan Kerajinan Tangan",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "COSRX Low PH Goog Morning Gel Cleanser",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
            Product(
                "",
                "Celana Pendek Pria Jumbo Polos Kaos ",
                "https://picsum.photos/200/200",
                "5",
                "122",
                "400",
            ),
        )
    }

    val categoryChild by lazy {
        listOf(
            CategoryChildModel(
                category_name = "bahan pokok",
            ), CategoryChildModel(
                category_name = "permen",
            ), CategoryChildModel(
                category_name = "makanan ringan ",
            ), CategoryChildModel(
                category_name = "makanan instan",
            )
        )
    }

    val minuman by lazy {
        listOf(
            CategoryChildModel(category_name = "marimas"),
            CategoryChildModel(category_name = "kopi"),
            CategoryChildModel(category_name = "josua"),
            CategoryChildModel(category_name = "segar sari"),
        )
    }

    val book by lazy {
        listOf(
            CategoryChildModel(category_name = "b indo"),
            CategoryChildModel(category_name = "matematika"),
            CategoryChildModel(category_name = "b jawa"),
            CategoryChildModel(category_name = "primbon"),
        )
    }

    val categoryMain by lazy {
        listOf(
            CategoryParentModel(
                category_name = "makanan",
                CategoryChild = categoryChild,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "true"
            ),
            CategoryParentModel(
                category_name = "minuman",
                CategoryChild = minuman,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "false"
            ),
            CategoryParentModel(
                category_name = "book",
                CategoryChild = book,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "false"
            ),
            CategoryParentModel(
                category_name = "makanan",
                CategoryChild = categoryChild,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "true"
            ),
            CategoryParentModel(
                category_name = "minuman",
                CategoryChild = minuman,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "false"
            ),
            CategoryParentModel(
                category_name = "book",
                CategoryChild = book,
                category_icon = "https://picsum.photos/200/200",
                is_selected = "false"
            ),

            )
    }

    val storeFilter by lazy {
        listOf(
            StoreFilterModel(name = "Semua Produk", is_selected = true, filter_code = "semua"),
            StoreFilterModel(name = "Terpopuler", is_selected = false, filter_code = "terpopuler"),
            StoreFilterModel(name = "Terbaru", is_selected = false, filter_code = "terbaru"),
            StoreFilterModel(
                name = "Harga Terbaik",
                is_selected = false,
                filter_code = "harga_terbaik"
            ),
        )
    }

    val sellerFilterUrutan by lazy {
        listOf(
            StoreFilterModel(name = "A-Z", is_selected = true, filter_code = "a-z"),
            StoreFilterModel(name = "Z-A", is_selected = false, filter_code = "z-a"),
            StoreFilterModel(name = "Terlaris", is_selected = false, filter_code = "terlaris"),
            StoreFilterModel(name = "Terpopuler", is_selected = false, filter_code = "terpopuler"),
            StoreFilterModel(name = "Termurah", is_selected = false, filter_code = "termurah"),
            StoreFilterModel(name = "Termahal", is_selected = false, filter_code = "termahal"),
        )
    }

    val sellerFilterTampilan by lazy {
        listOf(
            StoreFilterModel(name = "Grid", is_selected = true, filter_code = "grid"),
            StoreFilterModel(name = "List", is_selected = false, filter_code = "list"),
        )
    }

    val width by lazy { pref.getInt("screen_x") }
    val height by lazy { pref.getInt("screen_y") }

    val productImgDummy by lazy {
        listOf(
            ProductImg(imgUrl = "https://placeimg.com/100/100/any"),
            ProductImg(imgUrl = "https://placeimg.com/100/100/any"),
            ProductImg(imgUrl = "https://placeimg.com/100/100/any"),
            ProductImg(imgUrl = "https://placeimg.com/100/100/any"),
            ProductImg(imgUrl = "https://placeimg.com/100/100/any"),
            ProductImg(imgUrl = "https://placeimg.com/100/100/any"),
        )
    }

    val detailProductDummy by lazy {
        listOf(
            DetailProduct(
                name = "product dummy",
                description = " ini adalah deskripsi jadi ini adalah deskripsi ini adalah deskripsi jadi ini adalah deskripsiini adalah deskripsi jadi ini adalah deskripsiini adalah deskripsi jadi ini adalah deskripsiini adalah deskripsi jadi ini adalah deskripsi",
                product_img = productImgDummy
            )
        )
    }

    val masterListCategory = ArrayList<CategoryTable>()
    val listCategory by lazy { MutableLiveData<List<CategoryItem>>() }
    val listHomePopular by lazy { MutableLiveData<List<HomeProductItem>>() }
    val listHomeNewest by lazy { MutableLiveData<List<HomeProductItem>>() }
    val listHomeBestSeller by lazy { MutableLiveData<List<HomeProductItem>>() }
    val listHomeBestPrice by lazy { MutableLiveData<List<HomeProductItem>>() }
    val listHomeOtherVertical by lazy { MutableLiveData<List<HomeProductItem>>() }
    val listHomeOtherHorizontal by lazy { MutableLiveData<List<HomeProductItem>>() }
    val listRecomendation by lazy { MutableLiveData<List<HomeProductItem>>() }

    val listCategorySub by lazy { MutableLiveData<List<CategorySubItem>>() }

    val CountProductFilter by lazy { MutableLiveData<String>() }
    val selectedFilter by lazy { MutableLiveData<String>() }

    val listGoodieCardDetailItem by lazy { MutableLiveData<List<GoodiesListItem>>() }
    val goodieDetailProduk by lazy { MutableLiveData<GoodieDetailItem>() }
    val goodieDetailReview by lazy { MutableLiveData<DetailReviewItem>() }
    val goodieListReview by lazy { MutableLiveData<List<ListReviewItem>>() }
    val Merchant by lazy { MutableLiveData<MerchantItem>() }

    val MerchantGoodie by lazy { MutableLiveData<List<MerchantProductItem>>() }

    val listResultFilterGoodies by lazy { MutableLiveData<List<GoodiesListItem>>() }

    val screenWidth by lazy { pref.getInt("screen_x") }


    private val pageSize = 20
    var lastProduct = -1
    var hasNextProduct = true
    val LoadingShow by lazy { MutableLiveData(true) }


    val dummySuggest by lazy { MutableLiveData<List<String>>() }
    val dummyList2 by lazy { MutableLiveData<List<String>>() }


    fun loadHomePage() = viewModelScope.launch {
        try {
            val homepageData = apiService.loadHomepage().data
            listCategory.postValue(homepageData.category)
            listHomePopular.postValue(homepageData.populer)
            listHomeNewest.postValue(homepageData.newest)
            listHomeBestSeller.postValue(homepageData.bestseller)
            listHomeBestPrice.postValue(homepageData.bestprice)
            listHomeOtherHorizontal.postValue(homepageData.other_horizontal)
            listHomeOtherVertical.postValue(homepageData.other_vertical)
            listRecomendation.postValue(homepageData.recomendation)
        } catch (e: Exception) {
            Timber.e(e)
            errorString.postValue(e.message)
        }
    }

//    suspend fun loadCategory() = try {
//        masterListCategory.clear()
//        masterListCategory.addAll(apiService.loadCategory().data.also { listCategory.postValue(it) })
//        //set default true untuk bagian atas
//        masterListCategory.set(
//                0, CategoryItem(
//                masterListCategory[0].id,
//                masterListCategory[0].name,
//                masterListCategory[0].icon,
//                true,
//        )
//        )
//    } catch (e: Exception) {
//        Timber.e(e)
//        errorString.postValue(e.message)
//    }

    suspend fun loadCategorySub(categoryId: Int) = try {
        listCategorySub.postValue(apiService.loadCategorySub(categoryId, 1000, 0).data)
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }

    suspend fun loadCountProductFilter(filter: String) = try {
        LoadingShow.postValue(true)
        CountProductFilter.postValue(apiService.LoadCountProductFilter(filter).data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }

    suspend fun loadDummetySuggest() = try {
        dummySuggest.postValue(
            listOf("1", "1", "1", "1", "1")
        )
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }

    suspend fun loadDummyList2() = try {
        dummyList2.postValue(
            listOf("1", "1")
        )
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }

    val reviewFilter by lazy {
        listOf(
            ReviewFilter(
                1,
                "Semua",
                "all",
                true,
            ),
            ReviewFilter(
                2,
                "1",
                "1",
                false,
            ),
            ReviewFilter(
                3,
                "2",
                "2",
                false,
            ),
            ReviewFilter(
                4,
                "3",
                "4",
                false,
            ),
            ReviewFilter(
                5,
                "4",
                "4",
                false,
            ),
            ReviewFilter(
                6,
                "5",
                "5",
                false,
            ),
        )
    }

    //goodie filter store home
    suspend fun loadResulFilterGoodies(take: Int, skip: Int, filter: String) = try {
        listResultFilterGoodies.postValue(
            apiService.LoadResultFilterGoodies(
                take,
                skip,
                filter
            ).data
        )
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }

    fun listCategory(id_selected: Int) = db.store().getCategory()
        .toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchCategory(0, id_selected) } },
                {}
            ))

    private suspend fun fetchCategory(start: Int = 0, id_selected: Int) {
        if (start == lastProduct) {
            LoadingShow.postValue(false)
            return
        }

        lastProduct = start
        hasNextProduct = try {
            apiService.loadCategory()
                .run {

                    data.forEach {
                        Timber.e("load------------------")
                        var is_selected: Boolean = it.id == id_selected

                        db.store().insertCategory(
                            CategoryTable(
                                it.id,
                                it.name,
                                it.icon,
                                is_selected
                            ),
                        )
                    }
                    data.size >= pageSize
                }
        } catch (e: Exception) {
            Timber.e(e)
            false
        } finally {
            LoadingShow.postValue(false)
        }
    }

    private suspend fun resetSelected(id: Int) {
        db.store().unselectCategory()
        db.store().selectCategory(id)
    }

    fun listCategorySub(parent_id: Int) = db.store().getCategorySub(parent_id)
        .toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchCategorySub(0, parent_id) } },
                {
                    viewModelScope.launch {
                        val count = db.store().countCategorySub(parent_id)
                        Timber.e("count product: $count -- hasNext = $hasNextProduct")
                        if (hasNextProduct)
                            fetchCategorySub(count, parent_id)
                    }

                }
            ))


    private suspend fun fetchCategorySub(start: Int = 0, parent_id: Int) {
        if (start == lastProduct) {
            LoadingShow.postValue(false)
            return
        }

        lastProduct = start
        hasNextProduct = try {
            apiService.loadCategorySub(parent_id, pageSize, 0)
                .run {

                    data.forEach {
                        db.store().insertCategorySub(
                            CategorySubTable(
                                id = parent_id + it.id,
                                parent_id = parent_id,
                                sub_id = it.id,
                                name = it.name,
                                icon = "",
                            ),
                        )
                    }
                    data.size >= pageSize
                }
        } catch (e: Exception) {
            Timber.e(e)
            false
        } finally {
            LoadingShow.postValue(false)
        }
    }


    fun listgoodies(filter: String, position: String) = db.store().getProduct(filter, position)
        .toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchProduct(0, filter, position) } },
                {
                    viewModelScope.launch {
                        val count = db.store().countProduct(filter, position)
                        Timber.e("count product: $count -- hasNext = $hasNextProduct")
                        if (hasNextProduct)
                            fetchProduct(count, filter, position)
                    }
                }
            )
        )

    suspend fun fetchProduct(
        start: Int = 0,
        filter: String,
        position: String,
        categoryId: Int = 0,
        categorySubId: Int = 0
    ) {
        if (start == lastProduct) {
            LoadingShow.postValue(false)
            return
        }
        Timber.e("position $position")

        lastProduct = start
        if (position == "card_homepage") {
            hasNextProduct = try {
                apiService.loadGoodieCartDetail(pageSize, lastProduct, filter)
                    .run {
                        data.forEach {
                            db.store().insertProduct(
                                ProductTable(
                                    id = "${it.id}, ${it.name}",
                                    product_id = it.id,
                                    name = it.name,
                                    productFilter = filter,
                                    productPosition = position,
                                    price = it.price,
                                    rating = it.rating,
                                    product_sold = it.product_sold,
                                    image = it.main_image[0].image,
                                ),
                            )
                        }
                        data.size >= pageSize
                    }
            } catch (e: Exception) {
                Timber.e(e)
                false
            } finally {
                LoadingShow.postValue(false)
            }
        } else if (position == "filter_homepage") {
            hasNextProduct = try {
                apiService.LoadResultFilterGoodies(pageSize, lastProduct, filter)
                    .run {
                        data.forEach {
                            db.store().insertProduct(
                                ProductTable(
                                    id = "${it.id}, ${it.name}",
                                    product_id = it.id,
                                    name = it.name,
                                    productFilter = filter,
                                    productPosition = position,
                                    price = it.price,
                                    rating = it.rating,
                                    product_sold = it.product_sold,
                                    image = it.main_image[0].image,
                                ),
                            )
                        }
                        data.size >= pageSize
                    }
            } catch (e: Exception) {
                Timber.e(e)
                false
            } finally {
                LoadingShow.postValue(false)
            }
        } else if (position == "searchKey") { // untuk search page
            hasNextProduct = try {
                apiService.searchResult(pageSize, lastProduct, filter)
                    .run {
                        data.forEach {
                            db.store().insertProduct(
                                ProductTable(
                                    id = "${it.homeProductId}, ${it.name}",
                                    product_id = it.homeProductId,
                                    name = it.name,
                                    productFilter = filter,
                                    productPosition = position,
                                    price = it.price,
                                    rating = "",
                                    product_sold = 0,
                                    image = it.image,
                                ),
                            )
                        }
                        data.size >= pageSize
                    }
            } catch (e: Exception) {
                Timber.e(e)
                false
            } finally {
                LoadingShow.postValue(false)
            }
        } else if (position == "categoryProduct") {
            hasNextProduct = try {
                var subUrl = ""
                if (categorySubId == 0) {
                    subUrl = categorySubId.toString()
                } else {
                    subUrl = "/subcategories/$categorySubId"
                }
                apiService.loadCategoryProduct(categoryId, subUrl, pageSize, lastProduct)
                    .run {
                        data.forEach {
                            db.store().insertProduct(
                                ProductTable(
                                    id = "${it.homeProductId}, ${it.name}",
                                    product_id = it.homeProductId,
                                    name = it.name,
                                    productFilter = filter,
                                    productPosition = position,
                                    price = it.price,
                                    rating = "",
                                    product_sold = 0,
                                    image = it.image,
                                    category_id = it.category.id,
                                    sub_category_id = it.sub_category.id
                                ),
                            )
                        }
                        data.size >= pageSize
                    }
            } catch (e: Exception) {
                Timber.e(e)
                false
            } finally {
                LoadingShow.postValue(false)
            }

        }

    }

    //Goodie Detail Card
    suspend fun loadGoodieDetailCard(take: Int, skip: Int, filter: String) = try {
        listGoodieCardDetailItem.postValue(apiService.loadGoodieCartDetail(take, skip, filter).data)
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }

    //Detail Product
    suspend fun loadDetailGoodie(goodieId: Int) = try {
        LoadingShow.postValue(true)
        goodieDetailProduk.postValue(apiService.loadGoodieDetail(goodieId).data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }

    //produt review
    suspend fun loadDetailReview(goodieId: Int) = try {
        goodieDetailReview.postValue(apiService.loadGoodieDetailReview(goodieId).data)
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }

    suspend fun loadReviewList(goodieId: Int, starFilter: String) = try {
        goodieListReview.postValue(apiService.loadGoodieReview(goodieId, starFilter).data)
    } catch (e: Exception) {
        Timber.e(e)
        errorString.postValue(e.message)
    }


    // merchant goodie
    suspend fun loadMerchantGoodie(sellerId: Int) = try {
        LoadingShow.postValue(true)
        Merchant.postValue(apiService.loadMerchantGoodie(sellerId).data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e(e)
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    fun listProductMerchant(merchant_id: Int, merchantTabs: String) =
        db.store().getProductMerchant(merchant_id)
            .toLiveData(
                pageSize, boundaryCallback = PagedListBoundaryCallback(
                    {
                        viewModelScope.launch {
                            fetchproductMerchant(
                                0,
                                merchant_id,
                                merchantTabs
                            )
                        }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.store().countProductMerchant(merchant_id.toString())
                            Timber.e("count product: $count -- hasNext = $hasNextProduct")
                            if (hasNextProduct)
                                fetchproductMerchant(count, merchant_id, merchantTabs)
                        }

                    }
                ))


    suspend fun fetchproductMerchant(
        start: Int = 0,
        merchant_id: Int,
        merchantTabs: String
    ) { //merchnat tabs  -> best seller dan all

        if (start == lastProduct) {
            LoadingShow.postValue(false)
            return
        }

        lastProduct = start
        if (merchantTabs == "bestSeller") {
            hasNextProduct = try {
                apiService.productMerchantGoodie(merchant_id, pageSize, 0)
                    .run {
                        data.forEach {
                            db.store().insertProductMerchant(
                                ProductMerchantTable(
                                    "${merchant_id}, ${it.name}",
                                    merchant_id,
                                    it.id,
                                    it.name,
                                    it.description,
                                    it.price,
                                    it.stock,
                                    it.image,
                                    it.sold,
                                    it.rating,
                                ),
                            )
                        }
                        data.size >= pageSize
                    }
            } catch (e: Exception) {
                Timber.e(e)
                false
            } finally {
                LoadingShow.postValue(false)
            }
        } else {
            hasNextProduct = try {
                apiService.productBestSellerMerchantGoodie(merchant_id, pageSize, 0)
                    .run {
                        data.forEach {
                            db.store().insertProductMerchant(
                                ProductMerchantTable(
                                    "${merchant_id}, ${it.name}",
                                    merchant_id,
                                    it.id,
                                    it.name,
                                    it.description,
                                    it.price,
                                    it.stock,
                                    it.image,
                                    it.sold,
                                    it.rating,
                                ),
                            )
                        }
                        data.size >= pageSize
                    }
            } catch (e: Exception) {
                Timber.e(e)
                false
            } finally {
                LoadingShow.postValue(false)
            }
        }


    }


//    suspend fun loadProductMerchantGoodie(goodieId: Int) = try {
//        LoadingShow.postValue(true)
//        MerchantGoodie.postValue(apiService.productMerchantGoodie(goodieId).data)
//    } catch (e: Exception) {
//        LoadingShow.postValue(false)
//        Timber.e(e)
//        errorString.postValue(e.message)
//    }finally {
//        LoadingShow.postValue(false)
//    }


//    suspend fun loadProductMerchantGoodieBestSeller(goodieId: Int) = try {
//        LoadingShow.postValue(true)
//        MerchantGoodie.postValue(apiService.productMerchantGoodie(goodieId).data)
//    } catch (e: Exception) {
//        LoadingShow.postValue(false)
//        Timber.e(e)
//        errorString.postValue(e.message)
//    }finally {
//        LoadingShow.postValue(false)
//    }

    val suggestProductLData by lazy { MutableLiveData<List<SuggestProductItem>>() }
    suspend fun loadSuggestProduk(keyword: String) = try {
        LoadingShow.postValue(true)
        suggestProductLData.postValue(apiService.suggestProduct(5, 0, keyword).data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e("suggestProductLData $e")
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    val suggestMerchantLData by lazy { MutableLiveData<List<SuggestMerchantItem>>() }
    suspend fun loadSuggestMerchant(keyword: String) = try {
        LoadingShow.postValue(true)
        suggestMerchantLData.postValue(apiService.suggestMerchant(50, 0, keyword).data)
    } catch (e: Exception) {
        LoadingShow.postValue(false)
        Timber.e("suggestProductLData $e")
        errorString.postValue(e.message)
    } finally {
        LoadingShow.postValue(false)
    }


    val isFiltered by lazy { MutableLiveData<Boolean>(false) }

    fun listSearchResult(filter: String, position: String) = db.store().getProduct(filter, position)
        .toLiveData(
            pageSize, boundaryCallback = PagedListBoundaryCallback(
                { viewModelScope.launch { fetchProduct(0, filter, position) } },
                {
                    viewModelScope.launch {
                        val count = db.store().countProduct(filter, position)
                        Timber.e("count product: $count -- hasNext = $hasNextProduct")
                        if (hasNextProduct)
                            fetchProduct(count, filter, position)
                    }
                }
            )
        )

    fun listCategoryResult(filter: String, position: String, categoryId: Int, categorySubId: Int) =
        db.store().getProductCategeory(filter, position, categoryId, categorySubId)
            .toLiveData(
                pageSize, boundaryCallback = PagedListBoundaryCallback(
                    {
                        viewModelScope.launch {
                            fetchProduct(
                                0,
                                filter,
                                position,
                                categoryId = categoryId,
                                categorySubId = categorySubId
                            )
                        }
                    },
                    {
                        viewModelScope.launch {
                            val count = db.store()
                                .countProductCategory(filter, position, categoryId, categorySubId)
                            Timber.e("count product: $count -- hasNext = $hasNextProduct")
                            if (hasNextProduct)
                                fetchProduct(
                                    0,
                                    filter,
                                    position,
                                    categoryId = categoryId,
                                    categorySubId = categorySubId
                                )
                        }
                    }
                )
            )

}