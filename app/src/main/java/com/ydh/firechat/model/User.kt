package com.ydh.firechat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var userId:String = "", var userName:String = "", var profileImage:String = ""): Parcelable