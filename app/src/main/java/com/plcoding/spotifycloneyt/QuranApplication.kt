package com.plcoding.spotifycloneyt

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp /* mark this class as your application class for dagger hilt because it internally needs
 some of the stuff we have in this class */
class QuranApplication : Application ()