package com.plcoding.spotifycloneyt.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "quranTable")
data class QuranModel (
    @PrimaryKey
    val mediaId:String = "",
    val title:String = "",
    val subtitle:String = "",
    val songUrl:String = "",
    val imageUrl:String = "",
    val type:String = ""
):Parcelable{
    constructor() : this("","","","","","")
}