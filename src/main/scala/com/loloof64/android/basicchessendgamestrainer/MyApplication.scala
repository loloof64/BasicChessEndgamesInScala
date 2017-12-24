package com.loloof64.android.basicchessendgamestrainer

import android.app.Application
import android.content.Context

class MyApplication extends Application(){

    override def onCreate() {
        super.onCreate()
        setApplicationContext(this)
    }

}

object MyApplication {
    def getApplicationContext() = appContext
    def setApplicationContext(ctx: Context) {
        appContext = ctx
    }

    var appContext: Context
}