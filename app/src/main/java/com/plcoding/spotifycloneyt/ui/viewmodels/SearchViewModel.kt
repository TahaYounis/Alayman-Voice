package com.plcoding.spotifycloneyt.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.other.Resource
import com.plcoding.spotifycloneyt.other.ResourcesForLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
): ViewModel() {

    private val _search = MutableLiveData<ResourcesForLiveData<List<QuranModel>>>()
    val search: LiveData<ResourcesForLiveData<List<QuranModel>>> = _search

    fun searchProduct(searchQuery: String){
         _search.postValue(ResourcesForLiveData.loading(null))
        sp(searchQuery).addOnCompleteListener {
            if (it.isSuccessful){
                val productsList = it.result!!.toObjects(QuranModel::class.java)
                _search.postValue(ResourcesForLiveData.success(productsList))
            }else{
                    _search.postValue(ResourcesForLiveData.error(it.exception.toString(),null))
            }
        }
    }

    fun sp(searchQuery: String) = firestore.collection("quran")
        .orderBy("title")
        .startAt(searchQuery)
        .endAt("\uF8FF+$searchQuery")
        .limit(5)
        .get()
}