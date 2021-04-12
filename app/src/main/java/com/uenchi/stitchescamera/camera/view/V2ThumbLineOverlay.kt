package com.uenchi.stitchescamera.camera.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.uenchi.stitchescamera.R
import com.uenchi.stitchescamera.camera.model.EditorPage


class V2ThumbLineOverlay(
        private val mOverlayThumbLineBar: V2OverlayThumbLineBar,
        val startTime: Long,
        var mDuration: Long,
        val thumbLineOverlayView: ThumbLineOverlayView,
        maxDuration: Long,
        minDuration: Long,
        private val mIsInvert: Boolean,
        private var mSelectedDurationChange: OnSelectedDurationChangeListener?
) {

    companion object {
        const val STATE_ACTIVE = 1
        const val STATE_FIX = 2
    }

    private var mState = 0
    private var mMinDuration = 2000000L
    private var mMaxDuration = 0L
    private var mDistance = 0

    private var mTailView: ThumbLineOverlayHandleView? = null
    private var mHeadView: ThumbLineOverlayHandleView? = null
    private var mSelectedMiddleView: View? = null
    private var mContext: Context? = null
    private var mOverlayContainer: ViewGroup? = null
    var uiEditorPage: EditorPage? = null
    private var middleViewColor = 0

    val overlayView: View?
        get() = mOverlayContainer

    init {
        this.mState = STATE_ACTIVE
        this.mMaxDuration = maxDuration
        this.mMinDuration = minDuration
        initView(startTime)
        invalidate()
    }

    private fun initView(time: Long) {
        var startTime = time
        mSelectedMiddleView = thumbLineOverlayView.getMiddleView()
        if (!mIsInvert) {
            when {
                mDuration < mMinDuration ->
                    mDuration = mMinDuration
                startTime == mMaxDuration -> {
                    startTime = mMaxDuration - 100000
                    mDuration = mMaxDuration - startTime
                }
                mDuration + startTime > mMaxDuration -> //如果动图时长+startTime比最大时长大，则要向前移动，保证不超出范围。
                    mDuration = mMaxDuration - startTime
            }
        }

        mSelectedDurationChange?.onDurationChange(startTime, startTime + mDuration, mDuration)
        mTailView = ThumbLineOverlayHandleView(thumbLineOverlayView.getTailView(), startTime)
        mHeadView = ThumbLineOverlayHandleView(thumbLineOverlayView.getHeadView(), mDuration + startTime)
        mOverlayContainer = thumbLineOverlayView.getContainer()
        mOverlayContainer?.tag = this
        setVisibility(false)
        mOverlayThumbLineBar.addOverlayView(mOverlayContainer, mTailView, this, mIsInvert)
        this.mContext = mSelectedMiddleView?.context
    }

    fun switchState(state: Int) {
        mState = state
        when (state) {
            STATE_ACTIVE -> {
                mTailView?.active()
                mHeadView?.active()
                mContext?.apply {
                    if (middleViewColor != 0) {
                        mSelectedMiddleView?.setBackgroundColor(middleViewColor)
                    } else {
                        mSelectedMiddleView?.setBackgroundColor(ContextCompat.getColor(this, R.color.color_66EF4B81))
                    }
                }
            }
            STATE_FIX -> {
                mTailView?.fix()
                mHeadView?.fix()
                mContext?.apply {
                    if (middleViewColor != 0) {
                        mSelectedMiddleView?.setBackgroundColor(middleViewColor)
                    } else {
                        mSelectedMiddleView?.setBackgroundColor(ContextCompat.getColor(this, R.color.color_66EF4B81))
                    }
                }
            }
        }
    }

    fun updateMiddleViewColor(middleViewColor: Int) {
        if (this.middleViewColor != middleViewColor) {
            this.middleViewColor = middleViewColor
            mSelectedMiddleView?.setBackgroundColor(middleViewColor)
        }

    }

    private fun invalidate() {
        // 首先根据duration 计算middleView 的宽度
        mDistance = mOverlayThumbLineBar.duration2Distance(mDuration)
        val layoutParams = mSelectedMiddleView?.layoutParams
        layoutParams?.width = mDistance
        mSelectedMiddleView?.layoutParams = layoutParams
        when (mState) {
            STATE_ACTIVE -> {
                mTailView?.active()
                mHeadView?.active()
                mContext?.apply {
                    if (middleViewColor != 0) {
                        mSelectedMiddleView?.setBackgroundColor(middleViewColor)
                    } else {
                        mSelectedMiddleView?.setBackgroundColor(ContextCompat.getColor(this, R.color.color_66EF4B81))
                    }
                }
            }
            STATE_FIX -> {
                mTailView?.fix()
                mHeadView?.fix()
                mContext?.apply {
                    if (middleViewColor != 0) {
                        mSelectedMiddleView?.setBackgroundColor(middleViewColor)
                    } else {
                        mSelectedMiddleView?.setBackgroundColor(ContextCompat.getColor(this, R.color.color_66EF4B81))
                    }
                }
            }
        }
    }

    fun setVisibility(isVisible: Boolean) {
        if (isVisible) {
            mTailView?.view?.alpha = 1f
            mHeadView?.view?.alpha = 1f
            mSelectedMiddleView?.alpha = 1f
        } else {
            mTailView?.view?.alpha = 0f
            mHeadView?.view?.alpha = 0f
            mSelectedMiddleView?.alpha = 0f
        }
    }

    fun requestLayout() {
        val layoutParams = mTailView?.view?.layoutParams as ViewGroup.MarginLayoutParams
        if (mIsInvert) {
            layoutParams.rightMargin = mOverlayThumbLineBar.calculateTailViewInvertPosition(mTailView)
        } else {
            layoutParams.leftMargin = mOverlayThumbLineBar.calculateTailViewPosition(mTailView)
        }
        mTailView?.view?.layoutParams = layoutParams
    }

    private fun setLeftMargin(view: View, leftMargin: Int) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.leftMargin = leftMargin
        view.requestLayout()
    }

    interface ThumbLineOverlayView {
        fun getContainer(): ViewGroup

        fun getHeadView(): View

        fun getTailView(): View

        fun getMiddleView(): View
    }

    interface OnSelectedDurationChangeListener {
        fun onDurationChange(startTime: Long, endTime: Long, duration: Long)
    }

    fun setOnSelectedDurationChangeListener(selectedDurationChange: OnSelectedDurationChangeListener) {
        mSelectedDurationChange = selectedDurationChange
    }

    fun updateDuration(duration: Long) {
        println("updateDuration: $duration")
        mDuration = duration
        invalidate()
//        requestLayout()
    }
}
