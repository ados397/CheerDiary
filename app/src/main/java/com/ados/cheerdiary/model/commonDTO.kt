package com.ados.cheerdiary.model

import com.ados.cheerdiary.R

data class ScheduleDTO(
    var isSelected: Boolean = false,
    var title: String? = null
) {}

data class CalendarDTO(
    var StartDate: Boolean = false,
    var EndDate: String? = null
) {}

data class AppDTO(
    var isSelected: Boolean = false,
    val packageName: String? = null,
    val appName: String? = null,
    val iconImage: String? = null
) {}

data class FanClubDTO(
    var isSelected: Boolean = false,
    val name: String? = null,
    val iconImage: String? = null,
    val level: Int? = 0,
    val exp: Double? = 0.0,
    val master: String? = null,
    val count: Int? = 0
) {}

data class MemberDTO(
    var isSelected: Boolean = false,
    val name: String? = null,
    val contribution: Int? = 0,
    val position: POSITION? = POSITION.MEMBER,
    val isCheckout: Boolean? = false
) {
    enum class POSITION {
        MASTER, SUB_MASTER, MEMBER
    }

    fun getPositionString(): String {
        var positionString = ""
        when (position) {
            POSITION.MASTER -> positionString = "클럽장"
            POSITION.SUB_MASTER -> positionString =  "부클럽장"
            POSITION.MEMBER -> positionString =  "클럽원"
        }
        return positionString
    }

    fun getPositionImage(): Int {
        var positionImage = 0
        when (position) {
            POSITION.MASTER -> positionImage = R.drawable.medal_icon_09
            POSITION.SUB_MASTER -> positionImage =  R.drawable.medal_icon_48
            POSITION.MEMBER -> positionImage =  R.drawable.medal_icon_39
        }
        return positionImage
    }

    fun getCheckoutImage(): Int {
        return if (isCheckout!!)
            R.drawable.checked
        else
            R.drawable.cancel
    }
}

data class SignUpDTO(
    var isSelected: Boolean = false,
    var isChecked: Boolean = false,
    val name: String? = null
) {}