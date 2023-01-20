package id.onklas.app.pages.sekolah.store

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(data: CategoryTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(data: List<CategoryTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorySub(data: CategorySubTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorySub(data: List<CategorySubTable>)

    @Query("select * from store_category ")
    fun getCategory(): DataSource.Factory<Int, CategoryTable>

    @Query("select * from store_category where is_selected = 'true'")
    fun getSelectedCategory(): DataSource.Factory<Int, CategoryTable>


    @Query("select id from store_category where is_selected = 'true'")
    suspend fun getSelected(): Int

    @Query("update store_category set is_selected = 'false' ")
    suspend fun unselectCategory()

    @Query("update store_category set is_selected = 'true' where id = :category_id")
    suspend fun selectCategory(category_id: Int)


    @Query("Delete from store_category ")
    fun deleteCategory()


    @Query("select * from store_categorySub where parent_id = :parent_id ")
    fun getCategorySub(parent_id: Int): DataSource.Factory<Int, CategorySubTable>

    @Query("select count(*) from store_categorySub where parent_id = :parent_id ")
    suspend fun countCategorySub(parent_id: Int): Int

    @Query("Delete from store_categorySub where parent_id = :parent_id")
    fun deleteCategorySub(parent_id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(data: ProductTable)

    @Update(entity = ProductTable::class)
    suspend fun updateProduct(data: ProductTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(data: List<ProductTable>)

    @Query("select * from store_product where merchant_id = :merchantId and product_id > 0")
    fun getProductByMerchant(merchantId: Int): DataSource.Factory<Int, ProductTable>

    @Query("select count(*) from store_product where merchant_id = :merchantId and product_id > 0")
    suspend fun countProductByMerchant(merchantId: Int): Int

    @Query("select * from store_product where productFilter = :filter and productPosition= :position ")
    fun getProduct(filter: String, position: String): DataSource.Factory<Int, ProductTable>

    @Query("select * from store_product where product_id = :productId limit 1")
    fun getSingleProduct(productId: Int): LiveData<ProductTable>

    @Query("select count(*) from store_product where productFilter = :filter and productPosition= :position  ")
    suspend fun countProduct(filter: String, position: String): Int

    @Query("Delete From store_product where productFilter = :filter and productPosition= :position and product_id  = :Id ")
    suspend fun deleteProduct(filter: String, position: String, Id: Int)

    @Query("select * from store_product where productFilter = :filter and productPosition= :position and category_id = :categoryId and sub_category_id  =:categorySub  ")
    fun getProductCategeory(
        filter: String,
        position: String,
        categoryId: Int,
        categorySub: Int
    ): DataSource.Factory<Int, ProductTable>

    @Query("select count(*) from store_product where productFilter = :filter and productPosition= :position and category_id = :categoryId and sub_category_id= :categorySub   ")
    suspend fun countProductCategory(
        filter: String,
        position: String,
        categoryId: Int,
        categorySub: Int
    ): Int

    // merchant product

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductMerchant(data: ProductMerchantTable)

    @Query("select * from store_merchant_product where merchant_id = :merchantId")
    fun getProductMerchant(merchantId: Int): DataSource.Factory<Int, ProductMerchantTable>

    @Query("select count(*) from store_merchant_product where merchant_id = :merchantId  ")
    suspend fun countProductMerchant(merchantId: String): Int

    @Query("Delete from store_merchant_product where merchant_id = :merchantId")
    fun deleteProductMerchant(merchantId: Int)
}