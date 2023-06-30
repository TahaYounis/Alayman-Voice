package com.plcoding.spotifycloneyt.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.plcoding.spotifycloneyt.data.remote.MyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Singleton

/* dependencies should only live as long as our service does because we don't them outside service  */
@Module
@InstallIn(ServiceComponent::class) // dependencies should only live as long as our service does
object ServiceModule {
    // inside service we play a music
    // provide our firebase music source inside service, so that will allow dagger hilt to be able to inject MyDatabase instance into our firebaseQuranSource
    @ServiceScoped
    @Provides
    fun provideMyDatabase() = MyDatabase()
    // we need audio attributes which save some meta information about our player
    // we don't create this in service class because it make it more messy
    @ServiceScoped /* used it instead @Singleton because we use service component, it mean that we will
    have the same instance of these audio attributes in our same service instance */
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()


    // the player that play the music
    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        //dagger hilt know our QuranApplication because we we used @HiltAndroidApp, so insert this context behind the scenes
        @ApplicationContext context:Context,
        /* dagger hilt behind the scenes automatically see provide fun for those audio attributes, so it know how to create those
        audio attributes because we gave it this manual, so it pass the instance attributes from .setContentType() - .setUsage
        as a parameters for audioAttribute below, and can simply used them */
        audioAttributes: AudioAttributes
    )= SimpleExoPlayer.Builder(context).build().apply {
        // we used apply to set audio attributes to that exoPlayer
        setAudioAttributes(audioAttributes, true)
        setHandleAudioBecomingNoisy(true) // pause our music player for example if user plugs in his headphones

    }

    //provide our music source later on, will be our firebase source
    @ServiceScoped
    @Provides
    fun provideDataSourceFactory(
        @ApplicationContext context: Context
    ) = DefaultDataSource.Factory(context)
}