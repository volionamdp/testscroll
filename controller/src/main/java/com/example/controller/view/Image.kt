package com.example.controller.view

import android.content.Context
import android.graphics.*
import android.util.Log
import com.bumptech.glide.Glide
import com.example.controller.data.ImageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class Image(val context: Context, var imageModel: ImageModel, val callback: Callback) {
    val rectImage: RectF = RectF()
    private val rectOrigin: RectF = RectF()
    private val paint: Paint = Paint()
    private var bmItem: Bitmap? = null
    private var bmTransition: Bitmap? = null
    private var viewWidth: Int = 1
    private var viewHeight: Int = 1
    private var rectOriginBm: Rect = Rect()
    private var rectDrawBm: RectF = RectF()
    private var widthImage: Float = 1f

    init {
        val color: Int = ((imageModel.endTime + imageModel.startTime) % 255).toInt()
        paint.color = Color.argb(
            100,
            (imageModel.startTime % 255).toInt(),
            (imageModel.endTime % 255).toInt(),
            color
        )
        updateTime()
        loadImage()
    }

    fun setViewSize(width: Int, height: Int) {
        rectOrigin.top = 100f
        rectOrigin.bottom = height.toFloat()
        viewWidth = width
        viewHeight = height
        widthImage = 2f * rectOrigin.height() / 3f
    }

    fun updateTime() {
        rectOrigin.left = imageModel.startTime * ControllerView.msToPx
        rectOrigin.right = imageModel.endTime * ControllerView.msToPx
    }

    fun updateMatrix(matrix: Matrix) {
        matrix.mapRect(rectImage, rectOrigin)
        if (!(rectImage.right < 0 || rectImage.left > viewWidth)) {
//            loadImage()
        }
    }

    fun checkTouch(x: Float, y: Float): TouchType {
        if (rectImage.contains(x, y)) {
            return TouchType.CENTER
        }
        if (abs(rectImage.left - x) < 80) return TouchType.START
        if (abs(rectImage.right - x) < 80) return TouchType.END

        return TouchType.NONE
    }

    fun draw(canvas: Canvas) {
        if (!(rectImage.right < 0 || rectImage.left > viewWidth)) {
            canvas.drawRect(rectImage, paint)
            bmItem?.let {
                val ratio = it.width.toFloat() / it.height
                var start = rectImage.left
                rectDrawBm.set(start, rectImage.top, start + widthImage, rectImage.bottom)
                val ratioDraw = rectDrawBm.width() / rectDrawBm.height()
                if (ratioDraw >= ratio) {
                    val space = ((it.height - (it.width / ratioDraw)) / 2f).toInt()
                    rectOriginBm.set(0, space, it.width, it.height - space)
                } else {
                    val space = ((it.width - (it.height * ratioDraw)) / 2f).toInt()
                    rectOriginBm.set(space, 0, it.width - space, it.height)
                }
                while (true) {
                    rectDrawBm.set(start, rectImage.top, start + widthImage, rectImage.bottom)
                    if (rectDrawBm.right > rectImage.right) {
                        rectDrawBm.right = rectImage.right
                        val newBmWidth =
                            (rectOriginBm.width() * (rectDrawBm.width() / widthImage)).toInt()
                        Log.d("kkkk", "draw: ${rectOriginBm.width()} ${newBmWidth}")
                        rectOriginBm.right = rectOriginBm.left + newBmWidth
                    }
                    if (rectDrawBm.right >= 0 && rectDrawBm.left <= viewWidth) {
                        canvas.drawBitmap(it, rectOriginBm, rectDrawBm, null)
                    }
                    Log.d("zet", "draw: $rectOriginBm  $rectDrawBm ")
                    start += rectDrawBm.width()
                    if (start >= rectImage.right || start >= rectImage.right) break
                }
            }
        }
    }

    private var isLoadBm: Boolean = false
    fun loadImage() {
//        Glide.with(context).asBitmap().load(imageModel.pathImage).into(object : CustomTarget<Bitmap>() {
//            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                bmItem = resource
//            }
//
//            override fun onLoadCleared(placeholder: Drawable?) {
//
//            }
//
//        })

        Log.d(
            "zzz", "loadImage: ${bmItem == null}  ${
                !isLoadBm && (bmItem == null ||
                        bmItem?.isRecycled == true ||
                        imageModel.transition.duration > 0 ||
                        bmTransition == null ||
                        bmTransition?.isRecycled == true)
            }"
        )
        if (!isLoadBm && (bmItem == null ||
                    bmItem?.isRecycled == true ||
                    imageModel.transition.duration > 0 ||
                    bmTransition == null ||
                    bmTransition?.isRecycled == true)
        ) {
            isLoadBm = true


            CoroutineScope(Dispatchers.Default).launch {
                if (bmItem == null || bmItem?.isRecycled == true) {
                    kotlin.runCatching {
                        val bitmap =
                            Glide.with(context).asBitmap().load(imageModel.pathImage)
                                .submit(100, 100).get()
                        rectOriginBm.set(0, 0, bitmap.width, bitmap.height)
                        withContext(Dispatchers.Main) {
                            bmItem = bitmap
                        }
                    }
                    callback.invalidate()
                }
//                if (imageModel.transition.duration > 0) {
//                    val nextImage = callback.getNextImage(this@Image)
//                    if (nextImage != null && (bmTransition == null || bmTransition?.isRecycled == true)) {
//                        kotlin.runCatching {
//                            Glide.with(context).asBitmap().load(nextImage.pathImage)
//                                .override(100, 100)
//                                .submit().get()
//                        }
//                    }
//                }
                isLoadBm = false
            }
        }
    }

    interface Callback {
        fun getNextImage(image: Image): ImageModel?
        fun invalidate()
    }

    enum class TouchType {
        NONE, START, END, CENTER
    }
}