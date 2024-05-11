package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.ven.assists.Assists
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.simple.step.Step.onStep
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager


class AutoTrade : StepImpl {


    fun onLoginScreen(): Pair<Boolean, AccessibilityNodeInfo?> {
        return onStep("android.view.View", "打卡")
    }

    fun onHomeScreen(): Pair<Boolean, AccessibilityNodeInfo?> {
        return onStep("com.hexin.plat.android:id/title", "首页")
    }

    fun onSelfScreen(): Pair<Boolean, AccessibilityNodeInfo?> {
        return onStep("android.widget.TextView", "同花顺自选")
    }

    fun onTradeScreen(): Pair<Boolean, AccessibilityNodeInfo?> {
        return onStep("com.hexin.plat.android:id/tab_a", "A股")
    }

    fun onTradeDetailScreen(): Pair<Boolean, AccessibilityNodeInfo?> {
        return onStep("android.view.View", "打卡")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onImpl(collector: StepCollector) {
        collector.next(Step.LAUNCH) {
            OverManager.log("准备启动${App.TARGET_APP_NAME}")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName(
                    App.TARGET_PACKAGE_NAME,
                    "${App.TARGET_PACKAGE_NAME}.Hexin"
                )
                Assists.service?.startActivity(this)

                StepManager.execute(AutoTrade::class.java, Step.AFTER_LAUNCH)

            }
        }.nextLoop(Step.AFTER_LAUNCH) {
            OverManager.log("已启动")




            false
        }
    }

}