package com.ados.cheerdiary.model

import com.ados.cheerdiary.R
import java.util.*

data class QuestionDTO(var stat: STAT? = STAT.INFO,
                       var title: String? = null,
                       var content: String? = null
) {
    enum class STAT {
        INFO, WARNING, ERROR
    }
}

data class CalendarDTO(
    var StartDate: Boolean = false,
    var EndDate: String? = null
) {}



data class SignUpDTO(
    var isSelected: Boolean = false,
    var isChecked: Boolean = false,
    val name: String? = null
) {}

data class WeekDTO(
    var week: Int? = 0,
    var startDate: Date? = null,
    val endDate: Date? = null
) {}