package com.ven.assists.simple.step

import android.view.accessibility.AccessibilityNodeInfo
import com.ven.assists.Assists


object Step {
    const val STEP_NONE = -1

    /**
     * 启动
     */
    const val LAUNCH = 1

    /**
     * 启动之后
     */
    const val AFTER_LAUNCH = 2

    /**
     * 登录
     */
    const val LOGIN = 3

    /**
     * 首页
     */
    const val HOME = 4

    /**
     * 自选
     */
    const val SELF_FOLLOW = 5
    fun onStep(idOrClassName: String, flagNodeText: String): Pair<Boolean, AccessibilityNodeInfo?> {
        var nodes = Assists.findById(idOrClassName)
            .filter { an -> an.text.isNullOrBlank().not() }
            .filter { an -> an.text.contains(flagNodeText) }
        if (nodes.isEmpty()) {
            nodes = Assists.findByTags(idOrClassName)
                .filter { an -> an.text.isNullOrBlank().not() }
                .filter { an -> an.text.contains(flagNodeText) }
        }
        val b = nodes.isNotEmpty()
        return Pair(b, if (b) nodes.first() else null)
    }

}