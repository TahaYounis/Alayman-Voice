package com.plcoding.spotifycloneyt.other

/* this class recommended by google to be used to wrap around our network responses and that will be a generic
class to differentiate between successful and error responses and also help us to handle the loading state
so when we make a response that we can show a progress bar while that response is processing and when get the answer
we can use that class to tell us whether that answer successful or an error, and handle that error or show successful response */
// out in kotlin means we can pass kind of the parent classes of this parameters we pass
//data class Resources<out T> (
//    val status: Status,
//    val data: T?, // the data that resources holds
//    val message: String?) {
//    /* we will wrap this resources around anything for example our list of songs that we loaded that data
//    be the list of songs, so we have access to that in our fragments and activities */
//
//    companion object{
//        fun <T> success(data: T?) = Resources(Status.SUCCESS, data, null)
//        fun <T> error(message: String, data: T?) = Resources(Status.ERROR,data,message)
//        fun <T> loading(data: T?) = Resources(Status.LOADING, data,null) // for example if we implement a caching mechanism then could already have the data that comes from th cache while we load the data that comes from the remote data source
//        fun <T> unspecified() = Resources(Status.Unspecified,null,null) // for example if we implement a caching mechanism then could already have the data that comes from th cache while we load the data that comes from the remote data source
//    }
//}
//enum class Status{
//    SUCCESS,
//    ERROR,
//    LOADING,
//    Unspecified
//}

// <T> generic class mean can receive any data type
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
){
    class Success<T>(data: T?): Resource<T>(data)
    class Error<T>(message: String): Resource<T>(message = message)
    class Loading<T>: Resource<T>()
    class Unspecified<T>: Resource<T>()
}