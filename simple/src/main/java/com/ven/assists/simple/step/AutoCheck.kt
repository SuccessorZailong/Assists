package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.service.autofill.Validators.and
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.input
import com.ven.assists.Assists.paste
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager
import java.time.Clock
import java.util.Calendar

class AutoCheck : StepImpl {

    private fun onStep(idOrClassName: String, text: String): Pair<Boolean, AccessibilityNodeInfo?> {
        var nodes = Assists.findById(idOrClassName)
            .filter { an -> an.text.isNullOrBlank().not() }
            .filter { an -> an.text.contains(text) }
        if (nodes.isEmpty()) {
            nodes = Assists.findByTags(idOrClassName)
                .filter { an -> an.text.isNullOrBlank().not() }
                .filter { an -> an.text.contains(text) }
        }
        val b = nodes.isNotEmpty()
        return Pair(b, if (b) nodes.first() else null)
    }

    /**
     * <移动打卡页,打卡>
     */
    private fun onStep5(): Pair<Boolean, AccessibilityNodeInfo?> {

        return onStep("android.view.View", "打卡")
    }

    /**
     * <移动打卡页,正常>
     */
    private fun onStep6(): Pair<Boolean, AccessibilityNodeInfo?> {

        return onStep("android.view.View", "正常")
    }

    /**
     * <i9首页,移动打卡>
     */
    private fun onStep4(): Pair<Boolean, AccessibilityNodeInfo?> {

        return onStep("android.widget.TextView", "移动打卡")
    }


    /**
     * <工作台页面,i9移动端>
     */
    private fun onStep3(): Pair<Boolean, AccessibilityNodeInfo?> {


        return onStep("android.view.View", "i9")
    }

    /**
     * <应用首页,工作台>
     */
    private fun onStep2(): Pair<Boolean, AccessibilityNodeInfo?> {

        return onStep("com.alibaba.android.rimet:id/home_bottom_tab_text", "工作台")
    }

    private fun onStepLogin(): Pair<Boolean, AccessibilityNodeInfo?> {
        return onStep("com.alibaba.android.rimet:id/tv", "登录")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onImpl(collector: StepCollector) {
        collector.next(Step.STEP_1) {
            OverManager.log("启动${App.TARGET_APP_NAME}")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName(
                    App.TARGET_PACKAGE_NAME,
                    "${App.TARGET_PACKAGE_NAME}.biz.LaunchHomeActivity"
                )
                Assists.service?.startActivity(this)

                StepManager.execute(AutoCheck::class.java, Step.STEP_2)


            }


        }.nextLoop(Step.STEP_2) {

            onStepLogin().apply {
                if (this.first) {
                    OverManager.log("进入登录页面")
                    val loginBtn = this.second
                    Assists.findById("com.alibaba.android.rimet:id/et_phone_input").first().apply {
                        if (this.text.equals(App.USER_NAME).not()) {

                            input(App.USER_NAME)
                        }
                    }
                    Assists.findById("com.alibaba.android.rimet:id/et_pwd_login").first().apply {

                        if (this.text.equals(App.PWD).not()) {
                            input(App.PWD)
                        }
                    }
                    Assists.findById("com.alibaba.android.rimet:id/cb_privacy").first().apply {
                        if (!this.isChecked) {
                            click()
                        } else {
                            loginBtn?.let {
                                if (loginBtn.isEnabled) {
                                    OverManager.log("点击登录")
                                    loginBtn.findFirstParentClickable()?.click()
                                }
                            }

                        }
                    }

                }
            }




            onStep2().apply {
                if (this.first) {
                    OverManager.log("已进入应用首页")
                    this.second?.findFirstParentClickable()?.click()
                    StepManager.execute(AutoCheck::class.java, Step.STEP_3)
                    return@nextLoop true
                }
            }
            onStep3().apply {
                if (this.first) {
                    StepManager.execute(AutoCheck::class.java, Step.STEP_3)
                    return@nextLoop true
                }
            }
            onStep4().apply {
                if (this.first) {
                    StepManager.execute(AutoCheck::class.java, Step.STEP_4)
                    return@nextLoop true
                }
            }
            onStep5().apply {
                if (this.first) {
                    StepManager.execute(AutoCheck::class.java, Step.STEP_5)
                    return@nextLoop true
                }
            }
            onStep6().apply {
                if (this.first) {
                    StepManager.execute(AutoCheck::class.java, Step.STEP_6)
                    return@nextLoop true
                }
            }



            if (0f == it.loopSurplusSecond) {
                StepManager.execute(this::class.java, Step.STEP_1)
            }

            false
        }.nextLoop(Step.STEP_3) {
            onStep3().apply {
                if (this.first) {
                    OverManager.log("已进入工作台")
                    this.second?.findFirstParentClickable()?.click()
                    StepManager.execute(AutoCheck::class.java, Step.STEP_4)
                    return@nextLoop true
                } else {
                    OverManager.log("无法获取i9移动端")
                }
            }
            if (0f == it.loopSurplusSecond) {
                StepManager.execute(this::class.java, Step.STEP_2)
            }

            false

        }.nextLoop(Step.STEP_4) {
            onStep4().apply {
                if (this.first) {
                    OverManager.log("进入i9移动端")
                    this.second?.findFirstParentClickable()?.click()
                    this.second?.click()
                    StepManager.execute(AutoCheck::class.java, Step.STEP_5)
                    return@nextLoop true
                } else {
                    OverManager.log("无法进入i9移动端")
                    StepManager.execute(AutoCheck::class.java, Step.STEP_2)
                }
            }
            if (0f == it.loopSurplusSecond) {
                StepManager.execute(this::class.java, Step.STEP_3)
            }
            false
        }.nextLoop(Step.STEP_5) {
            onStep5().apply {
                if (this.first) {
                    OverManager.log("进入打卡页面")
                    StepManager.execute(AutoCheck::class.java, Step.STEP_6)
                    return@nextLoop true
                } else {
                    OverManager.log("无法进入打卡页面")
                }
            }




            if (0f == it.loopSurplusSecond) {
                StepManager.execute(this::class.java, Step.STEP_4)
            }
            false
        }.nextLoop(Step.STEP_6) {
            onStep6().apply {
                if (this.first) {
                    OverManager.log("进入打卡范围")
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)
                    if (hour > 17) {
                        OverManager.log("现在时间 $hour:$minute 下班了，可以打卡")
                        this.second?.findFirstParentClickable()?.click()
                    } else if (hour in 7..8) {
                        OverManager.log("现在时间 $hour:$minute  上班了，可以打卡")
                        this.second?.findFirstParentClickable()?.click()
                    } else {
                        OverManager.log("现在时间 $hour:$minute 工作时间，请勿打卡")
                    }
                } else {
                    OverManager.log("不在考勤范围内")
                }
            }
            if (0f == it.loopSurplusSecond) {
                StepManager.execute(this::class.java, Step.STEP_5)
            }
            false
        }
    }

}