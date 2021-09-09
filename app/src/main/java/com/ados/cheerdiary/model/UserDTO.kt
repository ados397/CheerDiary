package com.ados.cheerdiary.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDTO(
    var uid: String? = null,
    var userId: String? = null,
    var loginType: LoginType? = LoginType.EMAIL,
    var nickname: String? = null,
    var imageUrl: String? = null,
    var fanClubId: String? = null,
) : Parcelable {
    enum class LoginType {
        EMAIL, GOOGLE, FACEBOOK
    }
}