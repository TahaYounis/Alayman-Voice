package com.plcoding.spotifycloneyt.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.spotifycloneyt.exoplayer.QuranService
import com.plcoding.spotifycloneyt.exoplayer.QuranServiceConnection
import com.plcoding.spotifycloneyt.exoplayer.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    quranServiceConnection: QuranServiceConnection
) : ViewModel(){
    /* what will be specific in songViewModel is the current position in our player because we have seek bar
    in which we observe on the current player position we get from our quranService.
    in quranService we have curQuranDuration on the on hand which we continuously update in our songViewModel
    and on the other hand we will have livedata here for the current player position for which we will write a little
    extension fun for our playback state */

    private val playbackState = quranServiceConnection.playbackState

    private val _curQuranDuration = MutableLiveData<Long>()
    val curQuranDuration:LiveData<Long> = _curQuranDuration

    private val _curPlayerPosition = MutableLiveData<Long?>()
    val curPlayerPosition:LiveData<Long?> = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    /* we will run a coroutine that is bound to this songViewModel's lifecycle and this coroutine continuously update the value
    of player position and and songDuration so that immediately get the new values in our fragment later on */
    private fun updateCurrentPlayerPosition(){
        viewModelScope.launch {
            while(true){
                // this won't be infinite loop because this coroutine will be cancelled once this viewModel is cleared once we get out of song fragment this whe we didn't do that in mainViewModel
               val pos = playbackState.value?.currentPlaybackPosition
                if (curPlayerPosition.value != pos){
                    _curPlayerPosition.postValue(pos)
                    _curQuranDuration.postValue(QuranService.curChapterOfQuranDuration)
                }
                // we don't want to execute this while loop without any delay so add a little delay
                delay(100L)
            }
        }
    }
}