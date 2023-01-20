package id.onklas.app.pages

import id.onklas.app.db.MemoryDB
import id.onklas.app.utils.PreferenceClass
import javax.inject.Inject

open class BaseRepo @Inject constructor(
    val pref: PreferenceClass,
    val memoryDB: MemoryDB
) {

    val isStudent by lazy { pref.getBoolean("is_student") }
    val isTeacher by lazy { pref.getBoolean("is_teacher") }
}