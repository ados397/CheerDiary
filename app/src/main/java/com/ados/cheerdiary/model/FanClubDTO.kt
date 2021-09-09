package com.ados.cheerdiary.model

import com.ados.cheerdiary.R
import java.util.*

data class FanClubDTO(
    var isSelected: Boolean = false,
    val docName: String? = null,
    val name: String? = null,
    val description: String? = null,
    val iconImage: String? = null,
    val level: Int? = 0,
    val exp: Double? = 0.0,
    val masterUid: String? = null,
    val masterNickname: String? = null,
    val count: Int? = 0,
    val createTime: Date? = null
) {}

data class MemberDTO(
    var isSelected: Boolean = false,
    val userUid: String? = null,
    val userNickname: String? = null,
    val contribution: Int? = 0,
    val position: POSITION? = POSITION.MEMBER,
    val isCheckout: Boolean? = false
) {
    enum class POSITION {
        MASTER, SUB_MASTER, MEMBER, GUEST
    }

    fun getPositionString(): String {
        var positionString = ""
        when (position) {
            POSITION.MASTER -> positionString = "클럽장"
            POSITION.SUB_MASTER -> positionString = "부클럽장"
            POSITION.MEMBER -> positionString = "클럽원"
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