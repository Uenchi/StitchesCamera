package com.uenchi.stitchescamera.camera.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.uenchi.stitchescamera.R
import com.uenchi.stitchescamera.camera.model.MediaItem
import java.io.File

class ThumbRecyclerAdapter(
        private val context: Context,
        private val medias: MutableList<MediaItem>,
        private var mCount: Int, duration: Int,
        private val mScreenWidth: Int,
        private val thumbnailWidth: Int,
        private val thumbnailHeight: Int) : RecyclerView.Adapter<ThumbRecyclerAdapter.ThumbnailViewHolder>() {
    private var mInterval = duration / mCount.toLong()
    private val mCacheBitmaps = SparseArray<Bitmap>()
    private var mIndex = 0
    private var mCurrentIndex = -1

    fun setData(count: Int, duration: Int) {
        if (mInterval * count != duration.toLong() && mCacheBitmaps.size() != 0) {
            Log.i(TAG, "setData: clear cache")
            mCacheBitmaps.clear()
            cacheBitmaps()
        } else {
            mInterval = (duration / count).toLong()
        }
        this.mCount = count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_timeline_thumbnail, parent, false)
        return ThumbnailViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val bitmap = mCacheBitmaps.get(position)
        if (bitmap != null && !bitmap.isRecycled) {
            holder.mIvThumbnail?.setImageBitmap(bitmap)
            return
        }
        if (mIndex >= medias.size) {
            return
        }
        val media = medias[mIndex]
        var time = position * mInterval + mInterval / 2
        if (time > media.duration) {
            if (mCurrentIndex != mIndex) {
                mIndex++
                if (mIndex >= medias.size) {
                    return
                }
                mCurrentIndex = mIndex
            }
            if (mIndex > 0) {
                time -= medias[mIndex - 1].duration
            }
        }
        val file = File(medias[mIndex].path)
        val requestOptions = RequestOptions
                .frameOf(time * 1000)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
        holder.mIvThumbnail?.let {
            Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .apply(requestOptions)
                    .placeholder(R.mipmap.ic_launcher)
                    .override(thumbnailWidth, thumbnailHeight)
                    .into(it)
        }
    }

    override fun getItemCount(): Int {
        return if (mCount == 0) 0 else mCount
    }

    override fun onViewRecycled(holder: ThumbnailViewHolder) {
        super.onViewRecycled(holder)
        holder.mIvThumbnail?.setImageBitmap(null)
    }

    /**
     * 缓存缩略图
     */
    fun cacheBitmaps() {
        var index = 0
        var currentIndex = -1
        for (i in 1 until mCount + 1) {
            val media = medias[index]
            var time = (i - 1) * mInterval + mInterval / 2
            if (time > media.duration) {
                if (currentIndex != index) {
                    index++
                    if (index >= medias.size) {
                        return
                    }
                    currentIndex = index
                }
                if (index > 0) {
                    time -= medias[index - 1].duration
                }
            }
            val file = File(medias[index].path)
            val requestOptions = RequestOptions
                    .frameOf(time * 1000)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)

            Glide.with(context)
                    .asBitmap()
                    .apply(requestOptions)
                    .load(file)
                    .override(thumbnailWidth, thumbnailHeight)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadCleared(placeholder: Drawable?) {
                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            mCacheBitmaps.put(i, resource)
                        }
                    })
        }
    }

    class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mIvThumbnail: ImageView? = itemView.findViewById(R.id.iv_thumbnail)
    }

    companion object {
        private const val TAG = "ThumbRecyclerAdapter"
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_FOOTER = 2
        private const val VIEW_TYPE_THUMBNAIL = 3
    }
}
