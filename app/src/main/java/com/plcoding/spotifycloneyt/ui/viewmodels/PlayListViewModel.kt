package com.plcoding.spotifycloneyt.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.db.QuranDatabase
import com.plcoding.spotifycloneyt.other.ResourcesForLiveData

class PlayListViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val artist: String,
) : ViewModel() {

    private val _artistList = MutableLiveData<ResourcesForLiveData<List<QuranModel>>>()
    val artistList: LiveData<ResourcesForLiveData<List<QuranModel>>> = _artistList

    private val _albumList = MutableLiveData<ResourcesForLiveData<List<QuranModel>>>()
    val albumList: LiveData<ResourcesForLiveData<List<QuranModel>>> = _albumList


    init {
        fetchArtistList()
        fetchAlbumList()
    }

    fun fetchArtistList() {
            _artistList.postValue(ResourcesForLiveData.loading(null))

        firestore.collection("quran")
            .whereEqualTo("subtitle", artist).get()
            .addOnSuccessListener {
                val quran = it.toObjects(QuranModel::class.java)
                _artistList.postValue(ResourcesForLiveData.success(quran))

            }.addOnFailureListener {
                    _artistList.postValue(ResourcesForLiveData.error(it.message.toString(),null))
            }
    }
    fun fetchAlbumList() {
        _albumList.postValue(ResourcesForLiveData.loading(null))

        firestore.collection("quran")
            .whereEqualTo("type", artist).get()
            .addOnSuccessListener {
                val quran = it.toObjects(QuranModel::class.java)
                _albumList.postValue(ResourcesForLiveData.success(quran))

            }.addOnFailureListener {
                _albumList.postValue(ResourcesForLiveData.error(it.message.toString(),null))
            }
    }


}