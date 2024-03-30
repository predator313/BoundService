package com.example.boundservice.ui.viewmodel

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.boundservice.service.MyService

class MainActivityViewModel:ViewModel() {
    private val TAG="MainActivityViewModel"

    var isProgressUpdating=MutableLiveData<Boolean>()
        private set

    var mBinder=MutableLiveData<MyService.MyBinder?>()
        private set


    private val serviceConnection = object :ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e(TAG,"service connected")
            val binder = service as MyService.MyBinder
            mBinder.postValue(binder)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBinder.postValue(null)
        }

    }
    fun getServiceConnection():ServiceConnection{
        return serviceConnection
    }
}