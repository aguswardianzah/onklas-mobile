package id.onklas.app.pages.sekolah.store

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: CartTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<CartTable>)

    @Query("update cart set merchant_name = :merchantName where merchant_id = :merchantId and product_id = 0")
    suspend fun updateMerchant(merchantId: Int, merchantName: String)

    @Query("update cart set quantity = :quantity where merchant_id = :merchantId and product_id = :productId")
    suspend fun updateQuantity(quantity: Int, merchantId: Int, productId: Int)

    @Transaction
    @Query("select * from cart where showItem = 1 and selected = 1")
    suspend fun getSelected(): List<CartPaging>

    @Query("select count(*) from cart where showItem = 1")
    suspend fun countCart(): Int

    @Query("select count(*) from cart where showItem = 1 and selected = 1")
    suspend fun countSelected(): Int

    @Query("select count(*) from cart where merchant_id = :merchantId")
    suspend fun countByMerchant(merchantId: Int): Int

    @Query("select count(*) from cart where showItem = 1 and selected = 1 and merchant_id = :merchantId")
    suspend fun countSelectedByMerchant(merchantId: Int): Int

    @Query("update cart set selected = :selected where merchant_id = :merchantId and product_id = 0")
    suspend fun selectMerchant(merchantId: Int, selected: Boolean = true)

    @Query("update cart set selected = :selected where merchant_id = :merchantId")
    suspend fun selectByMerchant(merchantId: Int, selected: Boolean = true)

    @Query("update cart set selected = :selected where product_id = :productId")
    suspend fun selectByProduct(productId: Int, selected: Boolean = true)

    @Query("select * from cart where merchant_id = :merchantId and product_id = :productId")
    suspend fun getCart(merchantId: Int, productId: Int): CartTable?

    @Transaction
    @Query("select * from cart order by merchant_id, created desc, product_id")
    fun cartPagingSource(): DataSource.Factory<Int, CartPaging>

    @Delete
    suspend fun delete(item: CartTable)

    @Query("delete from cart where merchant_id = :merchantId")
    suspend fun deleteMerchant(merchantId: Int)

    @Query("delete from cart")
    suspend fun clear()

    @Query("select count(DISTINCT merchant_id) from cart where selected =:selected")
    suspend fun merchantSelected(selected: Boolean = true): Int
}