package com.ven.assists.simple

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.ven.assists.simple.step.AutoCheck

import com.ven.assists.stepper.StepManager

class App : Application() {

    companion object{
        const val TARGET_PACKAGE_NAME = "com.alibaba.android.rimet"
        const val TARGET_APP_NAME = "DingDing"
        const val USER_NAME = "15730661502"
        const val PWD = "5tgb%TGB."
    }
    override fun onCreate() {
        try {
            super.onCreate()
            Utils.init(this)
            StepManager.register(AutoCheck::class.java)
        }catch (e:Exception){
            e.printStackTrace()
        }

    }
}