package com.uenchi.stitchescamera.camera.view

import android.graphics.Point

class ThumbLineConfig {
    var thumbnailCount = 10
    var thumbnailPoint: Point? = null
    var screenWidth: Int = 0

    class Builder {
        private val mConfig = ThumbLineConfig()

        fun thumbPoint(point: Point): Builder {
            mConfig.thumbnailPoint = point
            return this
        }

        fun screenWidth(screenWidth: Int): Builder {
            mConfig.screenWidth = screenWidth
            return this
        }

        fun thumbnailCount(thumbnailCount: Int): Builder {
            mConfig.thumbnailCount = thumbnailCount
            return this
        }

        fun build(): ThumbLineConfig {
            return mConfig
        }

    }
}