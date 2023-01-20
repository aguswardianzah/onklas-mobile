package id.onklas.app.pages.perpus

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
@Keep
@Entity(
    tableName = "perpus_banner",
    indices = [Index(value = ["id"], name = "perpus_banner_idx", unique = true)]
)
data class PerpusBanner(
    @PrimaryKey @Json(name = "content_id") val id: Int = 0,
    @Json(name = "image") val file_path: String = "",
    val book_subject: String = "",
    val book_category: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class PerpusBannerResponse(val data: List<PerpusBanner> = emptyList())

@Entity(
    tableName = "book",
    indices = [
        Index("id", unique = true),
        Index("type", unique = false),
        Index("title", unique = false)
    ]
)
data class BookTable(
    @PrimaryKey val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val isbn: String = "",
    val page: String = "",
    val language: String = "",
    val author: String = "",
    val publisher: String = "",
    val published_at: String = "",
    val cover: String = "",
    val subject: String = "",
    val category: String = "",
    val type: String = ""
) {

    companion object {
        fun fromBookItem(item: BookResponseItem, type: String = "") = BookTable(
            item.book.id,
            item.book.title,
            item.book.description,
            item.book.isbn,
            item.book.page,
            item.book.language,
            item.book.author,
            item.book.publisher,
            item.book.published_at,
            item.book.cover,
            item.subject?.name ?: "",
            item.category?.name ?: "",
            type
        )
    }
}

@JsonClass(generateAdapter = true)
@Keep
data class BookItem(
    @Json(name = "book_id") val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val isbn: String = "",
    @Json(name = "number_of_page") val page: String = "",
    val language: String = "",
    val author: String = "",
    val publisher: String = "",
    val published_at: String = "",
    @Json(name = "cover_image_url") val cover: String = ""
)

@JsonClass(generateAdapter = true)
@Keep
data class BookSubjectItem(val id: Int = 0, val name: String = "")

@JsonClass(generateAdapter = true)
@Keep
data class BookCategoryItem(
    val id: Int = 0,
    val name: String = "",
    @Json(name = "book_subject_id") val subId: Int = 0
)

@JsonClass(generateAdapter = true)
@Keep
data class BookResponseItem(
    val book: BookItem = BookItem(),
    val subject: BookSubjectItem? = BookSubjectItem(),
    val category: BookCategoryItem? = BookCategoryItem()
)

@JsonClass(generateAdapter = true)
@Keep
data class BookResponse(val data: List<BookResponseItem> = emptyList())

data class BookSection(
    val id: Int = 0,
    val title: String = "",
    val books: List<BookTable> = emptyList()
)

@JsonClass(generateAdapter = true)
@Keep
data class BookStock(val stock: Int = 0, val available: Int = 0)

@JsonClass(generateAdapter = true)
@Keep
data class BookStockTab(val tab: BookStock = BookStock())

@JsonClass(generateAdapter = true)
@Keep
data class BookStockRespones(val data: BookStockTab = BookStockTab())

@JsonClass(generateAdapter = true)
@Keep
data class BookRent(
    @Json(name = "school_book_rent_id") val id: Int = 0,
    val start_at: String = "",
    val retur_at: String = "",
    val status: String = "",
    val code: String = "",
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val cover_image_url: String = "",
    val is_late: Boolean = false
)

@JsonClass(generateAdapter = true)
@Keep
data class BookRentResponse(
    val data: List<BookRent> = emptyList(),
    val total_rent: Int = 0,
    val total_rent_late: Int = 0
)

@Entity(
    tableName = "book_rent", indices = [
        Index("id", unique = true),
        Index("status", unique = false)
    ]
)
data class BookRentTable(
    @PrimaryKey val id: Int = 0,
    val start_at: String = "",
    val return_at_label: String = "",
    val return_at: Long = 0,
    val status: Int = 0, // 0: Ongoing, 1: Late, 2: Finished
    val code: String = "",
    val book_title: String = "",
    val book_author: String = "",
    val book_cover: String = ""
)