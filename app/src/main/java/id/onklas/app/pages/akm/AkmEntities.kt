package id.onklas.app.pages.akm

import androidx.room.*
import id.onklas.app.pages.tryout.ListTryoutData
import id.onklas.app.pages.tryout.TryoutExams
import java.util.*

object AkmStatus {
    const val AKM_STATUS_NEW = 0
    const val AKM_STATUS_DOWNLOADING = 1
    const val AKM_STATUS_DOWNLOADED = 2
    const val AKM_STATUS_FINISHED = 3
    const val AKM_STATUS_UPLOADED = 4
    const val AKM_STATUS_SCORED = 5
}

@Entity(
    tableName = "akm",
    indices = [
        Index("id", unique = true),
        Index("is_active", unique = false),
        Index("is_school_scope", unique = false)
    ]
)
data class AkmTable(
    @PrimaryKey val id: Int = 0,
    var status: Int = AkmStatus.AKM_STATUS_NEW,
    var is_active: Boolean = false,
    val name: String = "",
    val type: String = "",
    val level: Int = 0,
    val date_start: Date = Date(),
    val date_end: Date = Date(),
    val date_label: String = "",
    val time_label: String = "",
    var password: String = "",
    var score_status: String = "",
    var download_progress: Int = 0,
    var student_exam_id: Int = 0,
    var show_score: Boolean = true,
    var is_school_scope: Boolean = false,
    var most_significant_bits: Long? = null,
    var least_significant_bits: Long? = null,
    var exam_type: Int = ExamType.SCHOOL,
    var exam_template: String = "" // ditentukan
) {

    constructor(
        data: ListAkmData,
        status: Int = 0,
        name: String = "",
        type: String = "",
        dateStart: Date = Date(),
        dateEnd: Date = Date(),
        date_label: String = "",
        time_label: String = "",
        show_score: Boolean = true,
        exam_type: Int = ExamType.SCHOOL,
        exam_template: String = "",
    ) : this(
        data.id,
        status,
        data.isActive > 0,
        name,
        type,
        data.level,
        dateStart,
        dateEnd,
        date_label,
        time_label,
        score_status = data.status,
        show_score = show_score,
        exam_type = exam_type,
        exam_template = exam_template
    )

    companion object {
        fun tryoutToTabel(
            data: ListTryoutData,
            status: Int = 0,
            name: String = "",
            type: String = "",
            dateStart: Date = Date(),
            dateEnd: Date = Date(),
            date_label: String = "",
            time_label: String = "",
            show_score: Boolean = true,
            exam_type: Int = ExamType.SCHOOL,
            exam_template: String = "",
        ) = AkmTable(
            id = data.id,
            status = status,
            is_active = true, //data.isActive > 0,
            name = name,
            type = type,
            level = 0, // data.level,
            dateStart,
            dateEnd,
            date_label,
            time_label,
            score_status = "", //data.status,
            show_score = show_score,
            exam_type = exam_type,
            exam_template = exam_template
        )
    }

}

data class AkmIdStatus(val id: Int = 0, val status: Int = 0, val student_exam_id: Int = 0)
object ExamType {
    const val TRYOUT = 1
    const val SCHOOL = 2
}

@Entity(tableName = "akm_exams", indices = [Index("id", "schedule_id", unique = true)])
data class AkmExamsTable(
    @PrimaryKey(autoGenerate = true) val local_id: Int = 0,
    val id: Int = 0,
    val schedule_id: Int = 0,
    val name: String = "",
    var type: String = "",
    val grade: Int = 0,
    val author_nip: String = "",
    val author_name: String = "",
    val num_question: Int = 0,
    val score: Int = 0,
    var finished: Boolean = false,
    var show_child: Boolean = false,
) {

    constructor(schedule_id: Int, exam: AkmExams) : this(
        0,
        exam.id,
        schedule_id,
        exam.name,
        exam.type,
        exam.grade.toString().toIntOrNull() ?: 0,
        exam.author?.nip.orEmpty(),
        exam.author?.name.orEmpty(),
        exam.numberOfQuestions,
        exam.score
    )

    companion object {
        fun tryoutToTabel(
            schedule_id: Int, exam: TryoutExams
        ) = AkmExamsTable(
            0,
            exam.id,
            schedule_id,
            exam.name,
            exam.type,
            exam.grade.toString().toIntOrNull() ?: 0,
            exam.author?.nip.orEmpty(),
            exam.author?.name.orEmpty(),
            exam.numberOfQuestions,
            exam.score
        )
    }

}

data class AkmSchedule(
    @Embedded val schedule: AkmTable,
    @Relation(parentColumn = "id", entityColumn = "schedule_id")
    val exams: List<AkmExamsTable> = emptyList()
)

@Entity(tableName = "akm_instruction")
data class AkmInstructionTable(
    @PrimaryKey val id: Int = 0,
    val exam_id: Int = 0,
    val instruction: String = "",
    val description: String = "",
    val sequence: Int = 0,
    val random: Boolean = false,
    var num_question: Int = 0,
    var answered: Int = 0
) {

    constructor(examId: Int, data: AkmInstruction) : this(
        data.id,
        examId,
        data.instruction,
        data.description,
        data.sequence,
        data.isRandom > 0
    )
}

data class AkmExamInstruction(
    @Embedded val exam: AkmExamsTable,
    @Relation(parentColumn = "id", entityColumn = "exam_id")
    val instructions: List<AkmInstructionTable> = emptyList()
)

object AkmAnswerType {
    const val ANSWER_MULTIPLE_CHOICE_SINGLE_CORRECT = 0
    const val ANSWER_MULTIPLE_CHOICE_SINGLE_CORRECT_IMAGE = 1
    const val ANSWER_ESSAY = 2
    const val ANSWER_MULTIPLE_CHOICE_TRUE_FALSE = 3
    const val ANSWER_MULTIPLE_CHOICE_MULTIPLE_CORRECT = 4
    const val ANSWER_MULTIPLE_CHOICE_MULTIPLE_CORRECT_TABLE = 5
    const val ANSWER_PAIRING = 6
    const val ANSWER_PAIRING_IMAGE = 7
    const val ANSWER_ESSAY_NUM = 8
    const val ANSWER_ESSAY_WORD = 9
}

@Entity(tableName = "akm_question")
data class AkmQuestionTable(
    @PrimaryKey val id: Int = 0,
    val instruction_id: Int = 0,
    val question: String = "",
    val type: Int = AkmAnswerType.ANSWER_MULTIPLE_CHOICE_SINGLE_CORRECT,
    val type_label: String = "",
    var file_path: String = "",
    var score: Int = 0,
    var answered: Boolean = false,
    var answer_essay: String = ""
) {

    constructor(instruction_id: Int, data: AkmQuestion) : this(
        data.id,
        instruction_id,
        data.question,
        when {
            data.answerType == "MULTIPLE CHOICE" -> AkmAnswerType.ANSWER_MULTIPLE_CHOICE_SINGLE_CORRECT
            data.answerType == "STATEMENT" && data.answers.firstOrNull()?.showFalse == 1 -> AkmAnswerType.ANSWER_MULTIPLE_CHOICE_MULTIPLE_CORRECT_TABLE
            data.answerType == "STATEMENT" && data.answers.size == 1 -> AkmAnswerType.ANSWER_MULTIPLE_CHOICE_TRUE_FALSE
            data.answerType == "STATEMENT" -> AkmAnswerType.ANSWER_MULTIPLE_CHOICE_MULTIPLE_CORRECT
            data.answerType == "ESSAY" -> AkmAnswerType.ANSWER_ESSAY
            data.answerType == "PAIR" && data.answers.firstOrNull()?.firstStatement.isNullOrEmpty() -> AkmAnswerType.ANSWER_PAIRING_IMAGE
            data.answerType == "PAIR" -> AkmAnswerType.ANSWER_PAIRING
            data.answerType == "SHORT_ESSAY_NUM" -> AkmAnswerType.ANSWER_ESSAY_NUM
            data.answerType == "SHORT_ESSAY_WORD" -> AkmAnswerType.ANSWER_ESSAY_WORD
            else -> AkmAnswerType.ANSWER_ESSAY
        },
        data.answerType,
        data.image
//        data.score
    )
}

data class InstuctionQuestion(
    @Embedded val instruction: AkmInstructionTable,
    @Relation(parentColumn = "id", entityColumn = "instruction_id")
    val questions: List<AkmQuestionTable> = emptyList()
)

data class InstuctionQuestionAnswer(
    val instruction: AkmInstructionTable,
    val qa: List<QuestionAnswers> = emptyList()
)

@Entity(tableName = "akm_answer", indices = [Index("id", "question_id", unique = true)])
data class AkmAnswerTable(
    @PrimaryKey(autoGenerate = true) val local_id: Int = 0,
    val id: Int = 0,
    val question_id: Int = 0,
    val sequence: Int = 0,
    val answer: String = "",
    var file_path: String = "",
    val is_true: Boolean = false,
    val show_false: Boolean = false,
    val first_statement: String = "",
    val first_file_path: String = "",
    var second_statement: String = "",
    var second_file_path: String = "",
    var selected_id: Int = 0,
    var selected: Boolean = false
) {

    constructor(questionId: Int, sequence: Int, data: AkmAnswer) : this(
        0,
        data.id,
        questionId,
        sequence,
        data.answer,
        data.filePath,
        data.isTrue > 0,
        data.showFalse > 0,
        data.firstStatement,
        data.firstFilePath,
        data.secondStatement,
        data.secondFilePath,
        data.selected_id
    )
}

data class QuestionAnswers(
    @Embedded val question: AkmQuestionTable,
    @Relation(parentColumn = "id", entityColumn = "question_id")
    var answers: List<AkmAnswerTable> = emptyList()
)