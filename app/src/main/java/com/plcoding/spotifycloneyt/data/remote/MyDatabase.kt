package com.plcoding.spotifycloneyt.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.other.Constants.QURAN_COLLECTION
import kotlinx.coroutines.tasks.await

// Database class that accesses our firebase database and gets all quran list into our app
class MyDatabase {

    // to get instance to firebase fireStore
    private val fireStore = FirebaseFirestore.getInstance()
    // get reference to the quran collection that contain all quran
    private val quranCollection = fireStore.collection(QURAN_COLLECTION)

    /* implement the function that get all of our quran as a list of quran object
    suspend make fun suspendable so we can use coroutine in that fun because we do asynchronous option
    because we do a network call that takes time, and we don't want to execute that in a main thread
    that's why we execute in a coroutine */
    suspend fun getAllQuran(): List<QuranModel> {
        // we want to try to get quran from fireStore and if something goes wrong we want catch that exception
        return try {
            /* .get() to get all the documents in that collection
            . await() make .get() fun or network call a suspend fun so that will execute in coroutine
            this wait fun give us a object of type any, so this fun doesn't know yet what type of
            these object have that we get from fireStore database */
            quranCollection.get().await().toObjects(QuranModel::class.java)
        }catch (e: Exception){
            emptyList()
        }
    }
}