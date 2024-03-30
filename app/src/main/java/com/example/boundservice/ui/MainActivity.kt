package com.example.boundservice.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.boundservice.R
import com.example.boundservice.databinding.ActivityMainBinding
import com.example.boundservice.service.MyService
import com.example.boundservice.ui.viewmodel.MainActivityViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private  var service: MyService?=null
    private val viewModel:MainActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@MainActivity,
                R.color.white
            )))

        }
        binding.btn.setOnClickListener {
            toggleUpdate()
        }
        viewModel.mBinder.observe(this,Observer{binder->
//            Log.e("MainActivity","connected to the service")
//            service = binder?.getService()
            if(binder!=null){
                Log.e("MainActivity","connected to the service")
                service=binder.getService()
            }
            else{
                Log.e("MainActivity","unbind for the service")
                service=null
            }

        })
        viewModel.isProgressUpdating.observe(this,Observer{isUpdating->
            Log.e("aamir","inside observer block")
            val handler=Handler()
            val runnable=object :Runnable{
                override fun run() {
                    if(isUpdating){
                        if(viewModel.mBinder.value!=null){
                            if(service?.getProgress()==service?.getMaxValue()){
                                viewModel.isProgressUpdating.postValue(false)
                            }
                            binding.progressBar.progress=service?.getProgress().toString().toInt()
                            binding.progressBar.max=service?.getMaxValue().toString().toInt()

                            val progress=(100*service?.getProgress()!!/service?.getMaxValue()!!).toString()+"%"
                            binding.tv.text=progress
                            handler.postDelayed(this,100)

                        }
                    }else{
                        handler.removeCallbacks(this)
                    }
                }

            }
            if(isUpdating){
                binding.btn.text="Pause"
                handler.postDelayed(runnable,100)
            }
            else{
                if(service?.getProgress()==service?.getMaxValue()){
                    binding.btn.text="ReStart"
                }
                else binding.btn.text="Start"
            }


        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        startService()
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.mBinder!=null){
            unbindService(viewModel.getServiceConnection())
        }
    }

    private fun startService(){
        Intent(this,MyService::class.java).apply {
            startService(this)//this is inbuilt function

        }
        bindService()
    }
    private fun bindService(){
        Intent(this,MyService::class.java).apply {
            bindService(this,viewModel.getServiceConnection(),Context.BIND_AUTO_CREATE)
        }
    }

    private fun toggleUpdate(){
        service?.let {
            if(it.getProgress()==it.getMaxValue()){
                it.resetTask()
                binding.btn.text="Start"
            }
            else{
                if(it.getIsPaused()){
                    //service is paused
                    it.unPausedLongRunningTask()
                    viewModel.isProgressUpdating.postValue(true)

                }
                else{
                    it.pausedLongRunningTask()
                    viewModel.isProgressUpdating.postValue(false)

                }
            }
        }
    }

}