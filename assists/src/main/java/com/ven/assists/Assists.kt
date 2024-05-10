package com.ven.assists

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils


object Assists {

    //日志TAG
    var LOG_TAG = "assists_log"

    /**
     * 无障碍服务，未开启前为null，使用注意判空
     */
    @JvmStatic
    var service: AssistsService? = null

    val serviceListeners: ArrayList<AssistsServiceListener> = arrayListOf()

    //手势监听
    val gestureListeners: ArrayList<GestureListener> = arrayListOf()

    //手势执行延迟回调
    var gestureBeginDelay = 0L

    fun init() {
        LogUtils.getConfig().globalTag = LOG_TAG
    }

    /**
     * 打开无障碍服务设置
     */
    @JvmStatic
    fun openAccessibilitySetting() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        ActivityUtils.startActivity(intent)
    }

    /**
     *检查无障碍服务是否开启
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        return service != null
    }

    /**
     * 获取当前窗口所属包名
     */
    fun getPackageName(): String {
        return service?.rootInActiveWindow?.packageName?.toString() ?: ""
    }

    /**
     * 通过id查找所有符合条件元素
     */
    fun findById(id: String): List<AccessibilityNodeInfo> {
        return service?.rootInActiveWindow?.findById(id) ?: arrayListOf()
    }

    /**
     * 在当前元素范围下，通过id查找所有符合条件元素
     */
    fun AccessibilityNodeInfo?.findById(id: String): List<AccessibilityNodeInfo> {
        if (this == null) return arrayListOf()
        findAccessibilityNodeInfosByViewId(id)?.let {
            return it
        }
        return arrayListOf()
    }

    /**
     * 在当前元素范围下，通过文本查找所有符合条件元素
     */
    fun findByText(text: String): List<AccessibilityNodeInfo> {
        return service?.rootInActiveWindow?.findByText(text) ?: arrayListOf()
    }

    /**
     * 在当前元素范围下，通过文本查找所有符合条件元素
     */
    fun AccessibilityNodeInfo?.findByText(text: String): List<AccessibilityNodeInfo> {
        if (this == null) return arrayListOf()
        findAccessibilityNodeInfosByText(text)?.let {
            return it
        }
        return arrayListOf()
    }


    /**
     * 根据类型查找元素
     * @param className 完整类名，如[com.ven.assist.Assists]
     * @return 所有符合条件的元素
     */
    fun findByTags(className: String): List<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        getAllNodes().forEach {
            if (TextUtils.equals(className, it.className)) {
                nodeList.add(it)
            }
        }
        return nodeList
    }

    /**
     * 在当前元素范围下，根据类型查找元素
     * @param className 完整类名，如[com.ven.assist.Assists]
     * @return 所有符合条件的元素
     */
    fun AccessibilityNodeInfo.findByTags(className: String): List<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        getNodes().forEach {
            if (TextUtils.equals(className, it.className)) {
                nodeList.add(it)
            }
        }
        return nodeList
    }

    /**
     * 获取所有元素
     */
    fun getAllNodes(): ArrayList<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        service?.rootInActiveWindow?.getNodes(nodeList)
        return nodeList
    }

    /**
     * 获取指定元素下所有子元素
     */
    fun AccessibilityNodeInfo.getNodes(): ArrayList<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        this.getNodes(nodeList)
        return nodeList
    }

    /**
     * 递归获取所有元素
     */
    private fun AccessibilityNodeInfo.getNodes(nodeList: ArrayList<AccessibilityNodeInfo>) {
        nodeList.add(this)
        if (nodeList.size > 10000) return
        for (index in 0 until this.childCount) {
            getChild(index)?.getNodes(nodeList)
        }
    }

    /**
     * 查找第一个可点击的父元素
     */
    fun AccessibilityNodeInfo.findFirstParentClickable(): AccessibilityNodeInfo? {
        arrayOfNulls<AccessibilityNodeInfo>(1).apply {
            findFirstParentClickable(this)
            return this[0]
        }
    }

    /**
     * 查找可点击的父元素
     */
    private fun AccessibilityNodeInfo.findFirstParentClickable(nodeInfo: Array<AccessibilityNodeInfo?>) {
        if (parent.isClickable) {
            nodeInfo[0] = parent
            return
        } else {
            parent.findFirstParentClickable(nodeInfo)
        }
    }

    /**
     * 手势模拟，点或直线
     *
     * @param startLocation 开始位置，长度为2的数组，下标 0为 x坐标，下标 1为 y坐标
     * @param endLocation 结束位置
     * @param startTime 开始间隔时间
     * @param duration 持续时间
     * @return 执行手势动作总耗时
     */
    @JvmStatic
    fun gesture(
        startLocation: FloatArray,
        endLocation: FloatArray,
        startTime: Long,
        duration: Long,
    ): Long {
        gestureListeners.forEach { it.onGestureBegin(startLocation, endLocation) }
        val path = Path()
        path.moveTo(startLocation[0], startLocation[1])
        path.lineTo(endLocation[0], endLocation[1])
        return gesture(path, startTime, duration)
    }

    /**
     * 手势模拟
     * @param path 手势路径
     * @param startTime 开始间隔时间
     * @param duration 持续时间
     * @return 执行手势动作总耗时
     */
    @JvmStatic
    fun gesture(
        path: Path,
        startTime: Long,
        duration: Long,
    ): Long {
        val builder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, startTime, duration)
        val gestureDescription = builder.addStroke(strokeDescription).build()
        object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                gestureListeners.forEach { it.onGestureCompleted() }
                gestureListeners.forEach { it.onGestureEnd() }
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                gestureListeners.forEach { it.onGestureCancelled() }
                gestureListeners.forEach { it.onGestureEnd() }
            }
        }.apply {
            if (gestureBeginDelay == 0L) {
                service?.dispatchGesture(gestureDescription, this, null)
            } else {
                ThreadUtils.runOnUiThreadDelayed({
                    service?.dispatchGesture(gestureDescription, this, null)
                }, gestureBeginDelay)
            }
        }
        return gestureBeginDelay + startTime + duration
    }

    /**
     * 获取元素在屏幕中的范围
     */
    fun AccessibilityNodeInfo.getBoundsInScreen(): Rect {
        val boundsInScreen = Rect()
        getBoundsInScreen(boundsInScreen)
        return boundsInScreen
    }

    /**
     * 手势点击元素所处的位置
     */
    fun AccessibilityNodeInfo.gestureClick(): Long {
        val rect = getBoundsInScreen()
        return gestureClick(rect.left + 15F, rect.top + 15F, 10)
    }

    /**
     * 点击元素
     */
    fun AccessibilityNodeInfo.click() {
        performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * 手势长按元素所处的位置
     */
    fun AccessibilityNodeInfo.gestureLongClick(): Long {
        val rect = getBoundsInScreen()
        return gestureClick(rect.left + 15F, rect.top + 15F, 1000)
    }

    /**
     * 点击屏幕指定位置
     * @return 执行手势动作总耗时
     */
    fun gestureClick(
        x: Float,
        y: Float,
        duration: Long = 10
    ): Long {
        return gesture(
            floatArrayOf(x, y), floatArrayOf(x, y),
            0,
            duration,
        )
    }

    /**
     * 返回
     */
    fun back() {
        service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }


    /**
     * 回到主页
     */
    fun home() {
        service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    /**
     * 显示通知栏
     */
    fun notifications() {
        service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    }

    /**
     * 最近任务
     */
    fun tasks() {
        service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    /**
     * 粘贴文本
     */
    fun AccessibilityNodeInfo.paste(text: String?) {
        service?.let {

            val clip = ClipData.newPlainText("${System.currentTimeMillis()}", text)
            val clipboardManager =
                (it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            clipboardManager.setPrimaryClip(clip)
            clipboardManager.primaryClip
            performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }

    }

    /**
     * 逐个输入字符
     */
    fun AccessibilityNodeInfo.input(text: String?) {
        service?.let {
            val arguments = Bundle()

            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )

            performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }

    }


    /**
     * 根据基准分辨率宽度获取对应当前分辨率的x坐标
     */
    fun getX(baseWidth: Int, x: Int): Int {
        val screenWidth = ScreenUtils.getScreenWidth()
        return (x / baseWidth.toFloat() * screenWidth).toInt()
    }

    /**
     * 根据基准分辨率高度获取对应当前分辨率的y坐标
     */
    fun getY(baseHeight: Int, y: Int): Int {
        var screenHeight = ScreenUtils.getScreenHeight()
        if (screenHeight > baseHeight) {
            screenHeight = baseHeight
        }
        return (y.toFloat() / baseHeight * screenHeight).toInt()
    }

    /**
     * 控制台输出元素信息
     */
    fun AccessibilityNodeInfo.log(tag: String = LOG_TAG) {
        Log.d(
            tag, "text:$text / " +
                    "contentDescription:$contentDescription / " +
                    "viewIdResourceName:$viewIdResourceName / " +
                    "isFocused:$isFocused / " +
                    "isScrollable:$isScrollable / " +
                    "isEnabled:$isEnabled"
        )
    }
}