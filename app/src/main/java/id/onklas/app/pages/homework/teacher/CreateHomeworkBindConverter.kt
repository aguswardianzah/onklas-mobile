package id.onklas.app.pages.homework.teacher

import androidx.databinding.InverseMethod
import id.onklas.app.pages.homework.ClassRoomTable
import id.onklas.app.pages.theory.MapelTable

object CreateHomeworkBindConverter {

    val listClass by lazy { ArrayList<ClassRoomTable>() }

    @JvmStatic
    fun classId_to_name(classId: Int): String? =
        listClass.firstOrNull { it.id == classId }?.name

    @JvmStatic
    @InverseMethod(value = "classId_to_name")
    fun name_to_classId(name: String): Int? =
        listClass.firstOrNull { it.name == name }?.id

    fun classId_to_grade(classId: Int?) =
        listClass.firstOrNull { it.id == classId }?.grade
}