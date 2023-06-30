package com.plcoding.spotifycloneyt.ui.interfaces

import com.plcoding.spotifycloneyt.data.entities.QuranModel

interface SendDataFromFragmentToActivity {
    fun transferredDataFromFragment(list: List<QuranModel>, quranModel: QuranModel)
}