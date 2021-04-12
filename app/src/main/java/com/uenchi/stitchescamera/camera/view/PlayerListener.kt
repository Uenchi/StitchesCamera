package com.uenchi.stitchescamera.camera.view

interface PlayerListener {
    fun getCurrentDuration(): Long

    fun getDuration(): Long

    fun updateDuration(duration: Long)
}