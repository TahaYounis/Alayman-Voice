package com.plcoding.spotifycloneyt.other

/* help us to emit one-time events which is for example very useful when we want to show snackBar according
to such events because when we use livedata in android then when we rotate our device that livedata will
automatically emit again, and without that event class if there was an error message and we rotate the device
then the error would show again and this is wrong so we implement event class */
open class Event<out T>(private val data: T) {

    var hasBeenHandled = false // once we trigger event like snackBar we will set this to true then afterwards it won't emit this event again and instead emit null
        private set

    fun getContentIfNotHandle(): T?{
        return if(hasBeenHandled){ // if we already handled the event we will return null
            null
        }else{ // if we didn't handle the event yet return data
            hasBeenHandled = true
            data
        }
    }

    // if sometimes need to get the data event though it has already been handled then this fun return that here
    fun peekContent() = data
}