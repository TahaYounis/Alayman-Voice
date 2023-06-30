package com.plcoding.spotifycloneyt.di

import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.SwipeQuranAdapter
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.data.remote.MyDatabase
import com.plcoding.spotifycloneyt.db.QuranDao
import com.plcoding.spotifycloneyt.db.QuranDatabase
import com.plcoding.spotifycloneyt.exoplayer.QuranServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module /* Creating our Module for dagger hilt
 we just kind of give the manual how we create module/dependencies we want provide for our
 specific classes  of our servers, activity, fragments and so on, each module is an object so singleton */
// the app module will provide all dependencies that live as long as our application
@InstallIn(SingletonComponent::class) /* restrict the lifetime of the dependencies inside of this module
 to our whole application's lifetime so all of will be singletons and only have a single instance of these
 as long as our application lives and that's why we install that module into SingletonComponent  */
object AppModule {

    // we need to till to dagger how it should create the dependencies we need
    /* provide dependency that we need to throughout the entire lifetime of our app which is Glide instance
    so our image loading library will use we will use that as a singleton with some default options that
    we set here, and can simply inject that everywhere without needing to set those default options over and over again */
    // every fun in module provide something, so we will write provide in fun name
    @Singleton// make sure we have a single instance of this glide to prevent create new instance when use this fun
    @Provides// to till dagger we provide something
    fun provideQuranServiceConnection(
        // create object required to create glide instance
        //dagger hilt know our QuranApplication because we we used @HiltAndroidApp, so insert this context behind the scenes
        @ApplicationContext context: Context
    ) = QuranServiceConnection(context)

    @Singleton
    @Provides
    fun provideSwipeQuranAdapter() = SwipeQuranAdapter()

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .diskCacheStrategy(DiskCacheStrategy.DATA) // make sure our images are cached with glide
    )

    @Provides
    @Singleton
    fun provideFirebaseFireStoreDatabase() = Firebase.firestore


    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, QuranDatabase::class.java, "quran_db.db")
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideDao(db: QuranDatabase) = db.getQuranDao()

    @Provides
    @Singleton
    fun provideEntity() = QuranModel()

}