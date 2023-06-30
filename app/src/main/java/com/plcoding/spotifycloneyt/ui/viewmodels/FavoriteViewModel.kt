package com.plcoding.spotifycloneyt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.db.QuranDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

class FavoriteViewModel(
     db: QuranDatabase
):ViewModel() {


    val quranDatabase = db.getQuranDao()

    fun saveQuranToFavorite(quranModel: QuranModel) = viewModelScope.launch {
        quranDatabase.upsert(quranModel)
    }

    fun getSavedQuran() = quranDatabase.getAllQuran()

    fun deleteQuran(quranModel: QuranModel) = viewModelScope.launch {
        quranDatabase.deleteQuran(quranModel)
    }
}