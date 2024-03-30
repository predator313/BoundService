package com.example.boundservice.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

class MyService:Service() {
    private val binder=MyBinder()
    private var handler:Handler?=null
    private var maxValue:Int?=null
    private var progress:Int?=null
    private var isPaused:Boolean=true

    override fun onCreate() {
        super.onCreate()
        handler=Handler()
        progress=0
        maxValue=5000
        isPaused=true
    }
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    inner class MyBinder:Binder(){
        fun getService(): MyService = this@MyService

    }
    fun doLongRunningTask(){
        val runnable = object :Runnable{
            override fun run() {
                if(progress!!>=maxValue!! || isPaused){
                    Log.e("MyService","removing callback")
                    handler?.removeCallbacks(this)
                    pausedLongRunningTask()
                }
                else{
                    Log.e("MyService","update the progress+ $progress")
                    progress = progress!! + 100
                    handler?.postDelayed(this,100) //again start the process

                }
            }

        }
        handler?.postDelayed(runnable,100)  //start the whole process
    }

     fun pausedLongRunningTask() {
        isPaused=true
    }
     fun unPausedLongRunningTask(){
        isPaused=false
        doLongRunningTask()
    }
    fun getIsPaused()=isPaused
    fun getMaxValue()=maxValue
    fun getProgress()=progress
    fun resetTask(){
        progress=0
    }

    //very important method
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        //this function is used when the application is removed from recently used application list
        //means we swipe or clear the application from the list
        stopSelf()
        //we destroy the service when the application is swiped out from the background

    }
}