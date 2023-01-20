package id.onklas.app.pages.theoryteacher

import androidx.databinding.InverseMethod
import id.onklas.app.pages.homework.AssignmentSchedule
import id.onklas.app.pages.homework.ClassRoomTable
import id.onklas.app.pages.theory.GradeTable
import id.onklas.app.pages.theory.MajorItem
import id.onklas.app.pages.theory.MapelTable

object UploadMateriBindConverter {

    val listGrade by lazy { ArrayList<GradeTable>() }
    val listSubject by lazy { ArrayList<MapelTable>() }
    val listClass by lazy { ArrayList<ClassRoomTable>() }
    val listMajor by lazy { ArrayList<MajorItem>() }

    @JvmStatic
    fun gradeId_to_name(gradeId: Int): String =
        listGrade.firstOrNull { it.id == gradeId }?.name.orEmpty()

    @JvmStatic
    @InverseMethod(value = "gradeId_to_name")
    fun name_to_gradeId(name: String): Int =
        listGrade.firstOrNull { it.name == name }?.id ?: 0

    @JvmStatic
    fun subjectId_to_name(subjectId: Int): String =
        listSubject.firstOrNull { it.id.toInt() == subjectId }?.name.orEmpty()

    @JvmStatic
    @InverseMethod(value = "subjectId_to_name")
    fun name_to_subjectId(name: String): Int =
        listSubject.firstOrNull { it.name == name }?.id?.toInt() ?: 0

    @JvmStatic
    fun classId_to_name(classId: Int): String =
        listClass.firstOrNull { it.id == classId }?.name.orEmpty()

    @JvmStatic
    @InverseMethod(value = "classId_to_name")
    fun name_to_classId(name: String): Int =
        listClass.firstOrNull { it.name == name }?.id ?: 0

    @JvmStatic
    fun majorId_to_name(classId: Int): String =
        listMajor.firstOrNull { it.id == classId }?.name.orEmpty()

    @JvmStatic
    @InverseMethod(value = "majorId_to_name")
    fun name_to_majorId(name: String): Int =
        listMajor.firstOrNull { it.name == name }?.id ?: 0

    var listSchedule: List<AssignmentSchedule> = emptyList()

    @JvmStatic
    fun scheduleId_to_name(scheduleId: Int): String =
        listSchedule.firstOrNull { it.id == scheduleId }?.let {
            "[${it.time_plot.start_at} - ${it.time_plot.end_at}] ${it.subject.name}"
        }.orEmpty()

    @JvmStatic
    @InverseMethod(value = "scheduleId_to_name")
    fun name_to_scheduleId(name: String): Int =
        listSchedule.firstOrNull { "[${it.time_plot.start_at} - ${it.time_plot.end_at}] ${it.subject.name}" == name }?.id
            ?: 0
}