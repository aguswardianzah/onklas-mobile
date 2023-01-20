package id.onklas.app.di.modules

import com.squareup.moshi.*
import dagger.Module
import dagger.Provides
import dagger.Reusable
import java.lang.reflect.Type
import java.util.*

@Module
object MoshiModule {

    @Reusable
    @Provides
    @JvmStatic
    fun instance(): Moshi {
        return Moshi.Builder()
//            .add(object : JsonAdapter.Factory {
//                override fun create(
//                    type: Type,
//                    annotations: MutableSet<out Annotation>,
//                    moshi: Moshi
//                ): JsonAdapter<*>? =
//                    try {
//                        moshi.nextAdapter<Any>(this, type, annotations).lenient()
//                    } catch (_: Exception) {
//                        moshi.nextAdapter<Any>(this, type, annotations).lenient()
//                    }
//            })
//
            // add adapter to parse single object to list
            .add(object : JsonAdapter.Factory {
                override fun create(
                    type: Type,
                    annotations: Set<Annotation>,
                    moshi: Moshi
                ): JsonAdapter<Any>? {
                    val delegateAnnotations =
                        Types.nextAnnotations(annotations, ObjectToList::class.java)
                            ?: return null
                    if (Types.getRawType(type) != List::class.java) throw IllegalArgumentException("Only lists may be annotated with @SingleToArray. Found: $type")
                    val elementType = Types.collectionElementType(type, List::class.java)
                    val delegateAdapter: JsonAdapter<List<Any>> =
                        moshi.adapter(type, delegateAnnotations)
                    val elementAdapter: JsonAdapter<Any> = moshi.adapter(elementType)

                    return ObjectToListAdapter(delegateAdapter, elementAdapter)
                }
            })

            // add adapter to null string
            .add(object {
                @ToJson
                fun toJson(@NullToEmptyString value: String?): String? = value

                @FromJson
                @NullToEmptyString
                fun fromJson(reader: JsonReader): String? {
                    val result = if (reader.peek() === JsonReader.Token.NULL) {
                        reader.nextNull()
                    } else {
                        reader.nextString()
                    }

                    return result ?: ""
                }
            })

//            .add(object {
//                @ToJson
//                fun toJson(@JsonStringToInt value: Int?): Int? = value
//
//                @FromJson
//                @JsonStringToInt
//                fun fromJson(value: String?): Int = value?.toInt() ?: 0
//            })
            .build()
    }

    internal class ObjectToListAdapter(
        private val delegateAdapter: JsonAdapter<List<Any>>,
        private val elementAdapter: JsonAdapter<Any>
    ) : JsonAdapter<Any>() {

        override fun fromJson(reader: JsonReader): Any? =
            if (reader.peek() != JsonReader.Token.BEGIN_ARRAY)
                Collections.singletonList(elementAdapter.fromJson(reader))
            else
                delegateAdapter.fromJson(reader)

        override fun toJson(writer: JsonWriter, value: Any?) {}
    }
}

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NullToEmptyString

//@JsonQualifier
//@Retention(AnnotationRetention.RUNTIME)
//annotation class JsonStringToInt

//@JsonQualifier
//@Retention(AnnotationRetention.RUNTIME)
//annotation class NullToInt

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
internal annotation class ObjectToList
