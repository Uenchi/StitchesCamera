package com.uenchi.stitchescamera.camera.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.uenchi.stitchescamera.camera.model.EditorPage
import java.util.*
import kotlin.math.round

class V2OverlayThumbLineBar(
        context: Context,
        attrs: AttributeSet? = null
) : V2ThumbLineBar(context, attrs) {

    private val mOverlayList = ArrayList<V2ThumbLineOverlay>()

    /**
     * 添加overlay
     *
     * @param overlayView overlayView
     * @param tailView    tailView
     * @param overlay     overlay
     */
    fun addOverlayView(
            overlayView: View?,
            tailView: ThumbLineOverlayHandleView?,
            overlay: V2ThumbLineOverlay?,
            isIvert: Boolean) {

        addView(overlayView)
        overlayView?.post {
            val layoutParams = overlayView?.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.leftMargin = currDurationAsDis.toInt()
            overlayView.requestLayout()
            overlay?.setVisibility(true)
        }
    }

    fun addOverlay(
            time: Long,
            duration: Long,
            view: V2ThumbLineOverlay.ThumbLineOverlayView,
            minDuration: Long,
            isInvert: Boolean,
            uiEditorPage: EditorPage,
            listener: V2ThumbLineOverlay.OnSelectedDurationChangeListener? = null
    ): V2ThumbLineOverlay {
        var startTime = time
        if (startTime < 0) {
            startTime = 0
        }
        view.getContainer().tag = uiEditorPage
        mDuration = mLinePlayer?.getDuration() ?: 0
        val overlay = V2ThumbLineOverlay(this, startTime, duration, view, mDuration, minDuration, isInvert, listener)
        overlay.uiEditorPage = uiEditorPage
        mOverlayList.add(overlay)
        return overlay
    }

    /**
     * 实现和recyclerView的同步滑动
     * @param dx x的位移量
     * @param dy y的位移量
     */
    override fun onRecyclerViewScroll(dx: Int, dy: Int) {
        super.onRecyclerViewScroll(dx, dy)
    }

    /**
     * 实现和recyclerView的同步滑动
     */
    override fun onRecyclerViewScrollStateChanged(newState: Int) {
        super.onRecyclerViewScrollStateChanged(newState)
    }

    /**
     * 计算overlay尾部view左面的margin值
     * @param tailView view
     * @return int 单位sp
     */
    internal fun calculateTailViewPosition(tailView: ThumbLineOverlayHandleView?): Int {
        return if (tailView?.view != null) {
            (mThumbLineConfig.screenWidth / 2 - tailView.view.measuredWidth + duration2Distance(tailView.duration) - mCurrScroll).toInt()
        } else {
            0
        }
    }

    /**
     * 计算在倒放时overlay尾部view右边的margin值
     * @param tailView view
     * @return 单位sp
     */
    internal fun calculateTailViewInvertPosition(tailView: ThumbLineOverlayHandleView?): Int {
        return if (tailView?.view != null) {
            (mThumbLineConfig.screenWidth / 2 - tailView.view.measuredWidth - duration2Distance(tailView.duration) + mCurrScroll).toInt()
        } else {
            0
        }
    }

    /**
     * 时间转为尺寸
     *
     * @param duration 时长
     * @return 尺寸 pixel
     */
    internal fun duration2Distance(duration: Long): Int {
        val length = timelineBarViewWidth * duration.toFloat() * 1.0f / mDuration
        return round(length).toInt()
    }

    /**
     * 尺寸转为时间
     *
     * @param distance 尺寸 pixel
     * @return long duration
     */
    internal fun distance2Duration(distance: Float): Long {
        val length = mDuration * distance / timelineBarViewWidth
        return round(length).toLong()
    }

    /**
     * 清除指定
     */
    fun removeOverlay(overlay: V2ThumbLineOverlay?) {
        if (overlay != null) {
            Log.d(TAG, "remove TimelineBar Overlay : " + overlay.uiEditorPage)
            removeView(overlay.overlayView)
            mOverlayList.remove(overlay)
        }
    }

    fun removeOverlayByPages(vararg uiEditorPages: EditorPage) {
        if (uiEditorPages.isEmpty()) {
            return
        }
        val uiEditorPageList = Arrays.asList(*uiEditorPages)
        var i = 0
        while (i < childCount) {
            val childAt = getChildAt(i)
            if (childAt.tag is V2ThumbLineOverlay) {
                val thumbLineOverlay = childAt.tag as V2ThumbLineOverlay
                val uiEditorPage = thumbLineOverlay.uiEditorPage
                if (uiEditorPageList.contains(uiEditorPage)) {
                    removeOverlay(thumbLineOverlay)
                    i--//这里用i--，有时间换成迭代器来删除
                }
            }
            i++
        }
    }

    /**
     * 显示指定的overlay
     * @param uiEditorPage UIEditorPage
     */
    fun showOverlay(uiEditorPage: EditorPage?) {
        if (uiEditorPage == null) {
            return
        }
        val isCaption = uiEditorPage == EditorPage.FONT || uiEditorPage == EditorPage.SUBTITLE
        for (overlay in mOverlayList) {
            if (uiEditorPage === overlay.uiEditorPage) {
                overlay.overlayView?.visibility = View.VISIBLE
            } else if (isCaption && (overlay.uiEditorPage == EditorPage.SUBTITLE || overlay.uiEditorPage == EditorPage.FONT)) {
                overlay.overlayView?.visibility = View.VISIBLE
            } else {
                overlay.overlayView?.visibility = View.INVISIBLE
            }
        }
    }

    fun clearOverlay() {
        for (overlay in mOverlayList) {
            removeView(overlay.overlayView)
        }
        mOverlayList.clear()
    }

    public fun remoteLastOverlay() {
        if (mOverlayList.isNotEmpty()) {
            val lastIndex = mOverlayList.lastIndex
            val overlay = mOverlayList.get(lastIndex)
            removeView(overlay.overlayView)
            mOverlayList.removeAt(lastIndex)
        }
    }

    public fun remoteOverlayAt(pos: Int) {
        if (mOverlayList.isNotEmpty() && mOverlayList.size > pos) {
            val overlay = mOverlayList.get(pos)
            removeView(overlay.overlayView)
            mOverlayList.removeAt(pos)
        }
    }

    public fun remoteMultipleOverlay(size: Int) {
        for (index in 1..size) {
            remoteLastOverlay();
        }
    }

    companion object {
        private val TAG = V2OverlayThumbLineBar::class.java.name
    }

}
