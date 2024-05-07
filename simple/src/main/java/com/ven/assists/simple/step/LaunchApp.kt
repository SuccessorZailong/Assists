package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.paste
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager

class LaunchApp : StepImpl {
    override fun onImpl(collector: StepCollector) {
        collector.next(Step.STEP_1) {
            OverManager.log("启动应用-> ${App.TARGET_APP_NAME}")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName(
                    App.TARGET_PACKAGE_NAME,
                    "${App.TARGET_PACKAGE_NAME}.ui.MainActivity"
                )
                Assists.service?.startActivity(this)
            }
            StepManager.execute(this::class.java, Step.STEP_2)
        }.nextLoop(Step.STEP_2) {
            OverManager.log("检查是否已打开${App.TARGET_APP_NAME}：\n剩余时间=${it.loopSurplusSecond}秒")
            Assists.findByTags("android.widget.TextView").forEach { an ->
                if (an.text.contains("用户登录")) {
                    OverManager.log("an.text -> 前${an.text}后")
                    val formNodes = Assists.findByTags("android.widget.EditText")
                    formNodes[0].paste("13368438448")
                    formNodes[1].paste("13368438448")
                    Assists.findByTags("android.widget.TextView")
                        .first { btn -> btn.text.contains("登 录") }.findFirstParentClickable()
                        ?.click()
                    return@nextLoop true
                }
                if (an.text.contains("我的")) {
                    OverManager.log("已进入应用首页")
                    return@nextLoop true
                }

            }


            if (0f == it.loopSurplusSecond) {
                StepManager.execute(this::class.java, Step.STEP_1)
            }

            false
        }
    }
}