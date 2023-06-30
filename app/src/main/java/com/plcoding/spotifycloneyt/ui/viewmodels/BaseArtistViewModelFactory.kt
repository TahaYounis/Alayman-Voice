package com.plcoding.spotifycloneyt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

class BaseArtistViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val category: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayListViewModel(firestore, category) as T
    }
}